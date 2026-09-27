package com.ridelink.account.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

public class SwaggerThemeTransformer extends SwaggerIndexPageTransformer {

    private static final String CUSTOM_CSS = """

            body {
                background: #f8fafc;
            }

            .swagger-ui .topbar {
                background-color: #0f172a;
            }

            .swagger-ui .info .title {
                color: #0f172a;
                font-weight: 700;
            }

            .swagger-ui .opblock.opblock-get {
                background: #eff6ff;
                border-color: #3b82f6;
            }

            .swagger-ui .opblock.opblock-get .opblock-summary-method {
                background: #2563eb;
            }

            .swagger-ui .opblock.opblock-post {
                background: #f0fdf4;
                border-color: #22c55e;
            }

            .swagger-ui .opblock.opblock-post .opblock-summary-method {
                background: #16a34a;
            }

            .swagger-ui .opblock.opblock-patch {
                background: #fffbeb;
                border-color: #d97706;
            }

            .swagger-ui .opblock.opblock-patch .opblock-summary-method {
                background: #b45309;
            }

            .swagger-ui .opblock {
                border-radius: 6px;
                box-shadow: none;
            }

            .swagger-ui .opblock .opblock-summary-path {
                color: #1e293b;
                font-weight: 600;
            }

            .swagger-ui .opblock .opblock-summary-description {
                color: #475569;
            }

            .swagger-ui .btn.authorize {
                color: #2563eb;
                border-color: #2563eb;
                background: white;
            }

            .swagger-ui .btn.authorize svg {
                fill: #2563eb;
            }

            .swagger-ui .btn.execute {
                background: #2563eb;
                border-color: #2563eb;
                color: white;
            }

            """;

    public SwaggerThemeTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {

        super(
                swaggerUiConfig,
                swaggerUiOAuthProperties,
                swaggerWelcomeCommon,
                objectMapperProvider);
    }

    @Override
    public Resource transform(
            HttpServletRequest request,
            Resource resource,
            ResourceTransformerChain transformerChain)
            throws IOException {

        if ("swagger-ui.css".equals(resource.getFilename())) {

            String originalCss = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            return new TransformedResource(
                    resource,
                    (originalCss + "\n" + CUSTOM_CSS)
                            .getBytes(StandardCharsets.UTF_8));
        }

        return super.transform(
                request,
                resource,
                transformerChain);
    }
}