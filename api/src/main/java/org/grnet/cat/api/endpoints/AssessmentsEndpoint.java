package org.grnet.cat.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.cat.api.filters.Registration;
import org.grnet.cat.api.utils.CatServiceUriInfo;
import org.grnet.cat.api.utils.Utility;
import org.grnet.cat.constraints.NotFoundEntity;
import org.grnet.cat.dtos.AssessmentRequest;
import org.grnet.cat.dtos.AssessmentResponseDto;
import org.grnet.cat.dtos.InformativeResponse;
import org.grnet.cat.dtos.*;
import org.grnet.cat.repositories.AssessmentRepository;
import org.grnet.cat.services.AssessmentService;

@Path("/v1/assessments")
@Authenticated

public class AssessmentsEndpoint {

    @Inject
    Utility utility;
    @ConfigProperty(name = "server.url")
    String serverUrl;

    @Inject
    AssessmentService assessmentService;

    @Tag(name = "Assessment")
    @Operation(
            summary = "Request to create an assessment.",
            description = "This endpoint allows a validated user to create an assessment for a validation request." +
                    " The validated user should provide the necessary information to support their request.")
    @APIResponse(
            responseCode = "201",
            description = "Assessment creation successful.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = AssessmentResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Invalid request payload.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Assessment already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "501",
            description = "Not Implemented.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response create(@Valid @NotNull(message = "The request body is empty.") AssessmentRequest request, @Context UriInfo uriInfo) {

        var response = assessmentService.createAssessment(utility.getUserUniqueIdentifier(), request);
        var serverInfo = new CatServiceUriInfo(serverUrl.concat(uriInfo.getPath()));
        return Response.created(serverInfo.getAbsolutePathBuilder().path(String.valueOf(response.id)).build()).entity(response).build();
    }
    @Tag(name = "Assessment")
    @Operation(
            summary = "Get Assessment.",
            description = "Returns a specific assessment if it belongs to the user.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding assessment.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = AssessmentResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response getAssessment(@Parameter(
            description = "The ID of the assessment to retrieve.",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.NUMBER)) @PathParam("id")
                                  @Valid @NotFoundEntity(repository = AssessmentRepository.class, message = "There is no Assessment with the following id:") Long id) {

        var validations = assessmentService.getAssessment(utility.getUserUniqueIdentifier(), id);

        return Response.ok().entity(validations).build();
    }


    @Tag(name = "Assessment")
    @Operation(
            summary = "Update Assessment Json Document.",
            description = "Updates the json document for an assessment.")
    @APIResponse(
            responseCode = "200",
            description = "Assessment's json document updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = AssessmentResponseDto.class)))
    @APIResponse(
            responseCode = "400",
            description = "Invalid request payload.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @Path("/{id}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response updateAssessment(@Parameter(
            description = "The ID of the assessment to update.",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.NUMBER))
                                         @PathParam("id")
                                         @Valid @NotFoundEntity(repository = AssessmentRepository.class, message = "There is no assessment with the following id:") Long id,
                                     @Valid @NotNull(message = "The request body is empty.") UpdateAssessmentRequest updateAssessmentRequest) {

        var assessment =assessmentService.updateAssessment(id,utility.getUserUniqueIdentifier(),updateAssessmentRequest);

        return Response.ok().entity(assessment).build();
    }


}
