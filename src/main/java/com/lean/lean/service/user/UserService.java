package com.lean.lean.service.user;

import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.AddUserDTO;
import com.lean.lean.dto.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO registerUser(AddUserDTO user);
    List<UserDTO> getAllUsers();

    User getUserById(Long id);
}