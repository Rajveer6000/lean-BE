package com.lean.lean.service.user;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lean.lean.dao.LeanEntity;
import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import com.lean.lean.dto.AddUserDTO;
import com.lean.lean.dto.LeanCustomerRegResponse;
import com.lean.lean.dto.RentSavvyScoreDTO;
import com.lean.lean.dto.UserDTO;
import com.lean.lean.repository.LeanEntityRepository;
import com.lean.lean.repository.LeanUserRepository;
import com.lean.lean.repository.UserRepository;
import com.lean.lean.service.LeanReportService;
import com.lean.lean.util.LeanApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeanUserRepository leanUserRepository;

    @Autowired
    private  LeanApiUtil leanApiUtil;

    @Autowired
    private LeanEntityRepository leanEntityRepository;

    @Autowired
    private LeanReportService leanReportService;

    private final ObjectMapper objectMapper;

    private final ExecutorService apiExecutor = Executors.newFixedThreadPool(10);

    @Override
    public UserDTO registerUser(AddUserDTO addUserDTO) {
        User user = new User();
        user.setEmail(addUserDTO.getEmail());
        user.setPassword(addUserDTO.getPassword());
        user.setFirstName(addUserDTO.getFirstName());
        user.setLastName(addUserDTO.getLastName());
        user.setDateOfBirth(addUserDTO.getDateOfBirth());
        user.setGender(addUserDTO.getGender());
        user.setPhone(addUserDTO.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String accessToken = leanApiUtil.getAccessToken();

        LeanCustomerRegResponse leanCustomerRegResponse = leanApiUtil.createCustomerOnLean(savedUser, accessToken);
        LeanUser leanUser = new LeanUser();
        leanUser.setUser(savedUser);
        leanUser.setLeanUserId(leanCustomerRegResponse.getCustomer_id());
        leanUser.setCreatedAt(LocalDateTime.now());
        leanUser.setUpdatedAt(LocalDateTime.now());
        leanUserRepository.save(leanUser);

        return new UserDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getDateOfBirth(),
                savedUser.getGender(),
                savedUser.getPhone(),
                leanUser.getLeanUserId()
        );
    }

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        List<UserDTO> allUserDTOList = users.stream()
                .map(u -> {
                    // Fetch LeanUser for the current user
                    LeanUser leanUser = leanUserRepository.findByUser(u);

                    // Create the UserDTO with leanUserId if it exists
                    return new UserDTO(
                            u.getId(),
                            u.getEmail(),
                            u.getPassword(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getDateOfBirth(),
                            u.getGender(),
                            u.getPhone(),
                            leanUser != null ? leanUser.getLeanUserId() : null  // Add leanUserId if available
                    );
                })
                .toList();
        log.info("Mapped UserDTOs: {}", allUserDTOList);
        return allUserDTOList;
    }



    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

}