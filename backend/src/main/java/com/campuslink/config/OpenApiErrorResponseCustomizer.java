package com.campuslink.config;

import com.campuslink.common.result.ApiError;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 把错误响应写进 OpenAPI 契约。springdoc 只按 Controller 返回类型生成 200，
 * 而真实错误由 {@code GlobalExceptionHandler} 按 {@link ResultCode#getHttpStatus()} 返回，
 * 契约里完全看不到，前端只能按"只有 200"开发。
 *
 * <p>规则：端点用 {@link ErrorCodes} 声明业务码；有入参的操作补通用 400（含 1001）；所有操作补 500。
 * 状态码与提示语一律取自 {@link ResultCode}，此处不手写，避免契约与错误码枚举漂移。
 */
@Component
public class OpenApiErrorResponseCustomizer implements OperationCustomizer, OpenApiCustomizer {

    static final String ERROR_SCHEMA = "ApiError";

    private static final String JSON = "application/json";
    private static final int BAD_REQUEST = 400;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Map<Integer, List<ResultCode>> byStatus = declaredCodes(handlerMethod);
        ApiResponses responses = operation.getResponses() != null ? operation.getResponses() : new ApiResponses();

        List<ResultCode> badRequest = byStatus.remove(BAD_REQUEST);
        if (hasInput(operation) || badRequest != null) {
            List<ResultCode> codes = new ArrayList<>();
            if (hasInput(operation)) {
                codes.add(ResultCode.INVALID_PARAM);
            }
            if (badRequest != null) {
                codes.addAll(badRequest);
            }
            responses.addApiResponse(String.valueOf(BAD_REQUEST), errorResponse(codes));
        }

        byStatus.forEach((status, codes) ->
                responses.addApiResponse(String.valueOf(status), errorResponse(codes)));

        ResultCode internalError = ResultCode.INTERNAL_ERROR;
        responses.addApiResponse(String.valueOf(internalError.getHttpStatus()), errorResponse(List.of(internalError)));

        operation.setResponses(responses);
        return operation;
    }

    @Override
    public void customise(OpenAPI openApi) {
        ResolvedSchema resolved = ModelConverters.getInstance().readAllAsResolvedSchema(ApiError.class);
        resolved.referencedSchemas.forEach(openApi.getComponents()::addSchemas);
        openApi.getComponents().addSchemas(ERROR_SCHEMA, resolved.schema);
    }

    private Map<Integer, List<ResultCode>> declaredCodes(HandlerMethod handlerMethod) {
        Map<Integer, List<ResultCode>> byStatus = new LinkedHashMap<>();
        ErrorCodes annotation = handlerMethod.getMethodAnnotation(ErrorCodes.class);
        if (annotation != null) {
            for (ResultCode code : annotation.value()) {
                byStatus.computeIfAbsent(code.getHttpStatus(), status -> new ArrayList<>()).add(code);
            }
        }
        return byStatus;
    }

    private boolean hasInput(Operation operation) {
        return operation.getRequestBody() != null
                || (operation.getParameters() != null && !operation.getParameters().isEmpty());
    }

    private ApiResponse errorResponse(List<ResultCode> codes) {
        String description = codes.stream()
                .map(code -> code.getMessage() + "（" + code.getCode() + "）")
                .collect(Collectors.joining("；"));
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERROR_SCHEMA))));
    }
}
