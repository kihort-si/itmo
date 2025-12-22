package ru.itmo.is.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import ru.itmo.is.service.UploadFileService;
import ru.itmo.is.websocket.WebSocketMessageType;
import ru.itmo.is.websocket.WebSocketSessionManager;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/upload-file")
public class UploadFileController {
    @Inject
    private UploadFileService uploadFileService;
    @Inject
    private WebSocketSessionManager webSocketSessionManager;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formData = input.getFormDataMap();
            List<InputPart> files = formData.get("file");

            if (files == null || files.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .entity("No file")
                        .build();
            }

            InputPart filePart = files.get(0);

            String fileName = input.getFormDataPart("fileName", String.class, null);
            String fileSizeStr = input.getFormDataPart("size", String.class, null);
            Long fileSize = fileSizeStr != null ? Long.parseLong(fileSizeStr) : null;

            try (InputStream is = filePart.getBody(InputStream.class, null)) {
                uploadFileService.processJsonFile(is, fileName, fileSize);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .entity("Ошибка при импорте: " + e.getMessage())
                        .build();
            }

            input.close();

            webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
            webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
            webSocketSessionManager.broadcast(WebSocketMessageType.FILE);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().type(MediaType.TEXT_PLAIN + ";charset=UTF-8").entity("Upload error").build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFiles() {
        return Response.ok(uploadFileService.getAllFiles()).build();
    }
}
