package ru.itmo.is.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import ru.itmo.is.service.UploadFileService;
import ru.itmo.is.websocket.WebSocketMessageType;
import ru.itmo.is.websocket.WebSocketSessionManager;

import java.io.InputStream;

@Path("/upload-file")
public class UploadFileController {
    @Inject
    private UploadFileService uploadFileService;
    @Inject
    private WebSocketSessionManager webSocketSessionManager;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(MultipartBody body) {
        try {
            Attachment fileAttachment = body.getAttachment("file");
            
            if (fileAttachment == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .entity("No file")
                        .build();
            }

            String fileName = body.getAttachmentObject("fileName", String.class);
            String fileSizeStr = body.getAttachmentObject("size", String.class);
            Long fileSize = fileSizeStr != null ? Long.parseLong(fileSizeStr) : null;

            if (fileName == null || fileName.isEmpty()) {
                String contentDisposition = fileAttachment.getHeader("Content-Disposition");
                if (contentDisposition != null && contentDisposition.contains("filename")) {
                    int start = contentDisposition.indexOf("filename=") + 9;
                    int end = contentDisposition.indexOf("\"", start);
                    if (end == -1) {
                        end = contentDisposition.length();
                    }
                    fileName = contentDisposition.substring(start, end).replace("\"", "");
                }
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "uploaded_file.json";
                }
            }

            try (InputStream is = fileAttachment.getDataHandler().getInputStream()) {
                uploadFileService.processJsonFile(is, fileName, fileSize);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .entity("Ошибка при импорте: " + e.getMessage())
                        .build();
            }

            webSocketSessionManager.broadcast(WebSocketMessageType.MOVIE);
            webSocketSessionManager.broadcast(WebSocketMessageType.PERSON);
            webSocketSessionManager.broadcast(WebSocketMessageType.FILE);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().type(MediaType.TEXT_PLAIN + ";charset=UTF-8").entity("Upload error: " + e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFiles() {
        return Response.ok(uploadFileService.getAllFiles()).build();
    }

    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("id") Long id) {
        try {
            byte[] fileContent = uploadFileService.getFileContent(id);
            ru.itmo.is.model.File file = uploadFileService.getFileById(id);
            
            if (file == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .entity("File not found")
                        .build();
            }

            return Response.ok(fileContent)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .type(MediaType.TEXT_PLAIN + ";charset=UTF-8")
                    .entity("Error downloading file: " + e.getMessage())
                    .build();
        }
    }
}
