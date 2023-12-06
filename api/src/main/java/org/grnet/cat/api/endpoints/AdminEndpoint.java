package org.grnet.cat.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.cat.api.filters.Registration;
import org.grnet.cat.api.utils.Utility;
import org.grnet.cat.constraints.NotFoundEntity;
import org.grnet.cat.constraints.StringEnumeration;
import org.grnet.cat.dtos.InformativeResponse;
import org.grnet.cat.dtos.UpdateValidationStatus;
import org.grnet.cat.dtos.ValidationRequest;
import org.grnet.cat.dtos.ValidationResponse;
import org.grnet.cat.dtos.access.DenyAccess;
import org.grnet.cat.enums.ValidationStatus;
import org.grnet.cat.repositories.AssessmentRepository;
import org.grnet.cat.repositories.ValidationRepository;
import org.grnet.cat.services.UserService;
import org.grnet.cat.services.ValidationService;
import org.grnet.cat.services.assessment.JsonAssessmentService;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/admin")
@Authenticated
public class AdminEndpoint {

    /**
     * Injection point for the Validation service
     */
    @Inject
    ValidationService validationService;

    /**
     * Injection point for the JsonAssessment service
     */
    @Inject
    JsonAssessmentService assessmentService;

    /**
     * Injection point for the User Service
     */
    @Inject
    UserService userService;


    /**
     * Injection point for the Utility service
     */
    @Inject
    Utility utility;

    @Tag(name = "Admin")
    @Operation(
            summary = "Retrieve all validation requests.",
            description = "Retrieves a list of all validations requests submitted by users." +
                    "By default, the first page of 10 validation requests will be returned. You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "Successful response with the list of validation requests.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ValidationsEndpoint.PageableValidationResponse.class)))
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
    @Path("/validations")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response validations(@Parameter(name = "page", in = QUERY,
            description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.") @QueryParam("page") int page,
                                @Parameter(name = "size", in = QUERY,
                                        description = "The page size.") @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.")
                                @Max(value = 100, message = "Page size must be between 1 and 100.") @QueryParam("size") int size, @Valid @StringEnumeration(enumClass = ValidationStatus.class, message = "status") @QueryParam("status") @DefaultValue("") String status,
                                @Context UriInfo uriInfo) {

        var validations = validationService.getValidationsByPage(page-1, size, status, uriInfo);

        return Response.ok().entity(validations).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Update the information of a specific validation request.",
            description = "Updates the information of a specific validation request with the provided details.")
    @APIResponse(
            responseCode = "200",
            description = "Validation request was successfully updated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ValidationResponse.class)))
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
            description = "Validation request not found.",
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
    @PUT
    @Path("/validations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response updateValidation(@Parameter(
            description = "The Validation to be updated.",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.NUMBER))
                                         @PathParam("id") @Valid @NotFoundEntity(repository = ValidationRepository.class, message = "There is no Validation with the following id:") Long id,
                                     @Valid @NotNull(message = "The request body is empty.")ValidationRequest validationRequest) {

        var response = validationService.updateValidationRequest(id, validationRequest);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Update the status of a validation request.",
            description = "Updates the status of a validation request with the provided status.")
    @APIResponse(
            responseCode = "200",
            description = "The status of a validation request was successfully updated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ValidationResponse.class)))
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
            description = "Validation request not found.",
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
    @PUT
    @Path("/validations/{id}/update-status")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response updateTheStatusOfValidation(@Parameter(
            description = "The Validation to be updated.",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.NUMBER))
                                     @PathParam("id") @Valid @NotFoundEntity(repository = ValidationRepository.class, message = "There is no Validation with the following id:") Long id,
                                     @Valid @NotNull(message = "The request body is empty.") UpdateValidationStatus updateValidationStatus) {

        var response  = validationService.updateValidationRequestStatus(id, ValidationStatus.valueOf(updateValidationStatus.status), utility.getUserUniqueIdentifier());

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Get Validation Request.",
            description = "Returns a specific validation request.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding validation request.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ValidationResponse.class)))
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
    @Path("/validations/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response getValidationRequest(@Parameter(
            description = "The ID of the validation request to retrieve.",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.NUMBER)) @PathParam("id")
                                         @Valid @NotFoundEntity(repository = ValidationRepository.class, message = "There is no Validation with the following id:") Long id) {

        var validations = validationService.getValidationRequest(id);

        return Response.ok().entity(validations).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Delete private Assessment.",
            description = "Deletes a private assessment if it is not published.")
    @APIResponse(
            responseCode = "200",
            description = "Deletion completed.",
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @DELETE
    @Path("/assessments/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response deleteAssessment(@Parameter(
            description = "The ID of the assessment to be deleted.",
            required = true,
            example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                                     @Valid @NotFoundEntity(repository = AssessmentRepository.class, message = "There is no Assessment with the following id:") String id) {

        assessmentService.deletePrivateAssessment(id);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Assessment has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Retrieve a list of available users.",
            description = "This endpoint returns a list of users registered in the service. Each user object includes basic information such as their type and unique id. " +
                    " By default, the first page of 10 Users will be returned. You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "List of Users.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = UsersEndpoint.PageableUserProfile.class)))
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
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response usersByPage(@Parameter(name = "page", in = QUERY,
            description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.") @QueryParam("page") int page,
                                @Parameter(name = "size", in = QUERY,
                                        description = "The page size.") @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 20.")
                                @Max(value = 20, message = "Page size must be between 1 and 20.") @QueryParam("size") int size,
                                @Context UriInfo uriInfo) {

        var userProfile = userService.getUsersByPage(page-1, size, uriInfo);

        return Response.ok().entity(userProfile).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Restrict a user's access.",
            description = "Calling this endpoint results in the specified user being denied access to the API.")
    @APIResponse(
            responseCode = "200",
            description = "Successful operation.",
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
    @PUT
    @Path("/users/deny-access")
    @Produces(MediaType.APPLICATION_JSON)
    @Registration
    public Response denyAccess( @Valid @NotNull(message = "The request body is empty.") DenyAccess denyAccess) {

        userService.addDenyAccessRole(utility.getUserUniqueIdentifier(), denyAccess.userId, denyAccess.reason);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "deny_access role added successfully to the user. User now denied access to the API.";

        return Response.ok().entity(informativeResponse).build();
    }
}