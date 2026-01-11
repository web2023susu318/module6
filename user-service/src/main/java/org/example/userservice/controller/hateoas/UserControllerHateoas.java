package org.example.userservice.controller.hateoas;

import org.example.userservice.controller.UserController;
import org.example.userservice.dto.UserResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserControllerHateoas implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUserById(user.getUserId())).withSelfRel(),
                linkTo(methodOn(UserController.class).updateUser(user.getUserId(), null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(user.getUserId())).withRel("delete"),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );
    }
}