package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.dto.RouteDTO;
import com.ms.utils.mockpit.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/authenticate")
public class AuthenticationResource {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping
    public String generateAuthToken(@RequestBody RouteDTO.AuthRequest authRequest) throws MockpitApplicationException {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));
        } catch (Exception e){
            throw new MockpitApplicationException("Invalid user/password");
        }
        return jwtUtil.generateToken(authRequest.getUserName());
    }

}
