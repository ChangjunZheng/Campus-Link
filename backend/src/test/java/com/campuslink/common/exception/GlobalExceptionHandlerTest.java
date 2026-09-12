package com.campuslink.common.exception;

import com.campuslink.common.result.ApiError;
import com.campuslink.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户端错误不得落到 500 兜底：此前框架抛出的这些异常都没有专用处理器，
 * 一律变成 500 / 9999 并打全栈 error 日志，把客户端错误计入了 5xx。
 */
class GlobalExceptionHandlerTest {

    private static final String INTERNAL_DETAIL = "com.campuslink.internal.SecretType";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("业务异常按 ResultCode 的 httpStatus 返回")
    void businessExceptionUsesResultCodeStatus() {
        ResponseEntity<ApiError> response =
                handler.handleApiException(new ApiException(ResultCode.STUDENT_VERIFY_FAILED));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.STUDENT_VERIFY_FAILED.getCode());
        assertThat(response.getBody().message()).isEqualTo("学籍信息校验未通过");
        assertThat(response.getBody().traceId()).isNotBlank();
    }

    @Test
    @DisplayName("请求体不可解析 → 400 / 1001，且不回显解析器内部信息")
    void unreadableBodyIsBadRequestWithoutLeakingParserInternals() {
        HttpMessageNotReadableException e = new HttpMessageNotReadableException(
                "Unexpected character ('}' (code 125)): expected a value at [Source: " + INTERNAL_DETAIL + "]",
                emptyInputMessage());

        ResponseEntity<ApiError> response = handler.handleUnreadableBody(e);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.INVALID_PARAM.getCode());
        assertThat(response.getBody().message())
                .doesNotContain(INTERNAL_DETAIL)
                .doesNotContain("Unexpected character");
    }

    @Test
    @DisplayName("字段校验失败 → 400 / 1001 + 字段级提示")
    void validationFailureKeepsFieldLevelDetail() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "command");
        bindingResult.addError(new FieldError("command", "studentId", "学号必须为 9 位数字"));
        MethodArgumentNotValidException e = new MethodArgumentNotValidException(anyParameter(), bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(e);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.INVALID_PARAM.getCode());
        assertThat(response.getBody().message()).isEqualTo("studentId 学号必须为 9 位数字");
    }

    @Test
    @DisplayName("缺失必填查询参数 → 400 / 1001")
    void missingParameterIsBadRequest() {
        ResponseEntity<ApiError> response = handler.handleBadArgument(
                new MissingServletRequestParameterException("q", "String"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.INVALID_PARAM.getCode());
    }

    @Test
    @DisplayName("方法不支持 → 405 / 1002，并按 RFC 9110 带 Allow 头")
    void methodNotSupportedReturns405WithAllowHeader() {
        ResponseEntity<ApiError> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("GET", List.of("POST")));

        assertThat(response.getStatusCode().value()).isEqualTo(405);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.METHOD_NOT_ALLOWED.getCode());
        assertThat(response.getHeaders().getAllow()).containsExactly(HttpMethod.POST);
    }

    @Test
    @DisplayName("内容类型不支持 → 415 / 1003")
    void unsupportedMediaTypeReturns415() {
        ResponseEntity<ApiError> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException("Content type 'text/plain' not supported"));

        assertThat(response.getStatusCode().value()).isEqualTo(415);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.UNSUPPORTED_MEDIA_TYPE.getCode());
    }

    @Test
    @DisplayName("无此路由 → 404 / 1004")
    void unknownRouteReturns404() {
        ResponseEntity<ApiError> response = handler.handleNoHandler(
                new NoResourceFoundException(HttpMethod.GET, "api/v1/nonexistent", "/api/v1/nonexistent"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.ENDPOINT_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("未预期异常仍是 500 / 9999，不与客户端错误混淆")
    void unexpectedExceptionStaysInternalServerError() {
        ResponseEntity<ApiError> response = handler.handleUnexpected(new IllegalStateException("database down"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().code()).isEqualTo(ResultCode.INTERNAL_ERROR.getCode());
        assertThat(response.getBody().message()).isEqualTo("系统繁忙，请稍后再试");
    }

    @Test
    @DisplayName("错误响应 JSON 形状固定为 {code, message, traceId}，无 data 字段")
    void errorBodyHasNoDataField() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new ApiError(1001, "参数错误", "b208e820c94e"));

        assertThat(json).isEqualTo("{\"code\":1001,\"message\":\"参数错误\",\"traceId\":\"b208e820c94e\"}");
    }

    private static HttpInputMessage emptyInputMessage() {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return InputStream.nullInputStream();
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }

    private static MethodParameter anyParameter() throws NoSuchMethodException {
        Method method = Fixture.class.getDeclaredMethod("endpoint", String.class);
        return new MethodParameter(method, 0);
    }

    private static class Fixture {
        void endpoint(String command) {
        }
    }
}
