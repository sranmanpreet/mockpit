package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.domain.User;
import com.ms.utils.mockpit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void initUsers(){
        if(userRepository.findAll().size()==0){
            List<User> users = Stream.of(
                    new User("minaret", "password", "manpreet@abc.com"),
                    new User("admin", "admin", "admin@abc.com"),
                    new User("usera", "usera", "user@abc.com")
            ).collect(Collectors.toList());
            userRepository.saveAll(users);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findByUserName(username);
        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), new ArrayList<>());
    }
}

