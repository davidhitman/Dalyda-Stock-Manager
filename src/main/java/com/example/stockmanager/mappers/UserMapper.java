package com.example.stockmanager.mappers;

import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Users;

public class UserMapper {

    public static Users map(UserDto.SignupDto signupDto) {
        return new Users(signupDto.getFirstName(), signupDto.getLastName(), signupDto.getEmail(), signupDto.getPhoneNumber());
    }

    public static UserDto.ViewUserDto map(Users user) {
        return new UserDto.ViewUserDto(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getPassword(), user.getPhoneNumber(), user.getRole());
    }
}
