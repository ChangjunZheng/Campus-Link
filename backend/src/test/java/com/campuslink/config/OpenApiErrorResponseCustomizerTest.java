package com.campuslink.config;

import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 契约里的错误响应必须与 {@link ResultCode} 同源：状态码、提示语、错误体 schema 都不手写。
 */
class OpenApiErrorResponseCustomizerTest {

    private static final String ERROR_REF = "#/components/schemas/ApiError";

    private final OpenApiErrorResponseCustomizer customizer = new OpenApiErrorResponseCustomizer();

    @Test
    @DisplayName("有请求体的操作：补通用 400（含 1001）+ 声明的业务码 + 500")
    void addsGeneric400ForOperationWithRequestBody() throws Exception {
        Operation operation = new Operation()
                .requestBody(new RequestBody())
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("成功")));

        ApiResponses responses = customize("withBody", operation).getResponses();

        assertThat(responses).containsKeys("200", "400", "429", "500");
        assertThat(responses.get("400").getDescription()).contains("1001").contains("2101");
        assertThat(responses.get("429").getDescription()).contains("2103");
        assertThat(responses.get("500").getDescription()).contains("9999");
        assertThat(schemaRef(responses.get("400"))).isEqualTo(ERROR_REF);
    }

    @Test
    @DisplayName("无入参的操作不声明 400：它没有可校验的输入")
    void skips400ForOperationWithoutInput() throws Exception {
        ApiResponses responses = customize("withoutInput", new Operation().responses(new ApiResponses())).getResponses();

        assertThat(responses).doesNotContainKey("400").containsKeys("401", "404", "500");
        assertThat(responses.get("401").getDescription()).contains("4001");
        assertThat(responses.get("404").getDescription()).contains("2007");
    }

    @Test
    @DisplayName("查询参数也算入参，同样补 400")
    void treatsQueryParameterAsInput() throws Exception {
        Operation operation = new Operation()
                .addParametersItem(new Parameter().name("batch"))
                .responses(new ApiResponses());

        assertThat(customize("plain", operation).getResponses()).containsKeys("400", "500");
    }

    @Test
    @DisplayName("未声明业务码且无入参的操作只补 500；responses 为 null 时自建")
    void addsOnly500WhenNothingIsDeclared() throws Exception {
        ApiResponses responses = customize("plain", new Operation()).getResponses();

        assertThat(responses).containsOnlyKeys("500");
        assertThat(schemaRef(responses.get("500"))).isEqualTo(ERROR_REF);
    }

    @Test
    @DisplayName("ApiError 的 schema 由运行时类型反射生成：只有 code / message / traceId，没有 data")
    void registersErrorSchemaDerivedFromRuntimeType() {
        OpenAPI openApi = new OpenAPI().components(new Components());

        customizer.customise(openApi);

        assertThat(openApi.getComponents().getSchemas()).containsKey("ApiError");
        assertThat(openApi.getComponents().getSchemas().get("ApiError").getProperties())
                .containsKeys("code", "message", "traceId")
                .doesNotContainKey("data");
    }

    private Operation customize(String methodName, Operation operation) throws Exception {
        HandlerMethod handlerMethod =
                new HandlerMethod(new Fixture(), Fixture.class.getDeclaredMethod(methodName));
        return customizer.customize(operation, handlerMethod);
    }

    private static String schemaRef(ApiResponse response) {
        return response.getContent().get("application/json").getSchema().get$ref();
    }

    private static class Fixture {

        @ErrorCodes({ResultCode.STUDENT_VERIFY_FAILED, ResultCode.VERIFY_RATE_LIMITED})
        void withBody() {
        }

        @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.USER_NOT_FOUND})
        void withoutInput() {
        }

        void plain() {
        }
    }
}
