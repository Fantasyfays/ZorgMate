package com.example.zorgmate.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final InvoiceWebSocketHandler invoiceWebSocketHandler;

    public WebSocketConfig(InvoiceWebSocketHandler invoiceWebSocketHandler) {
        this.invoiceWebSocketHandler = invoiceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(invoiceWebSocketHandler, "/ws/invoices")
                .setAllowedOrigins("*");
    }
}
