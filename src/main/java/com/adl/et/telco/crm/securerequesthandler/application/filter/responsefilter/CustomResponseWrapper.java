package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper for HttpServletResponse that captures the response content for modification.
 * Allows reading and modifying the response body before it is sent to the client.
 */
@Slf4j
public class CustomResponseWrapper extends HttpServletResponseWrapper {
    private static final String LOG_PREFIX = "SRH|CustomResponseWrapper|";
    private final ByteArrayOutputStream byteArrayOutputStream;
    private ServletOutputStream servletOutputStream;
    private PrintWriter printWriter;

    public CustomResponseWrapper(HttpServletResponse response) {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        log.debug("{}Response wrapper initialized", LOG_PREFIX);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null) {
            log.error("{}getWriter() has already been called on this response", LOG_PREFIX);
            throw new IllegalStateException("getWriter() has already been called on this response");
        }
        if (servletOutputStream == null) {
            servletOutputStream = new ServletOutputStreamWrapper(byteArrayOutputStream);
            log.debug("{}ServletOutputStream created", LOG_PREFIX);
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (servletOutputStream != null) {
            log.error("{}getOutputStream() has already been called on this response", LOG_PREFIX);
            throw new IllegalStateException("getOutputStream() has already been called on this response");
        }
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8));
            log.debug("{}PrintWriter created", LOG_PREFIX);
        }
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (printWriter != null) {
            printWriter.flush();
        }
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        log.debug("{}Buffer flushed", LOG_PREFIX);
    }

    public byte[] getResponseAsByteArray() throws IOException {
        flushBuffer();
        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        log.debug("{}Response captured, size: {} bytes", LOG_PREFIX, responseBytes.length);
        return responseBytes;
    }

    private static class ServletOutputStreamWrapper extends ServletOutputStream {
        private final OutputStream outputStream;

        public ServletOutputStreamWrapper(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            outputStream.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException("Async write operations are not supported");
        }
    }
}
