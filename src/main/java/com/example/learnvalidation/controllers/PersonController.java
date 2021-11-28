package com.example.learnvalidation.controllers;

import com.example.learnvalidation.requests.PersonRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController(value = "RestController")
@RequestMapping(path = "/person")
public class PersonController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createPerson(@RequestBody @Valid PersonRequest request, BindingResult bindingResult) {

        if(bindingResult.hasErrors()){
            Map<String, String> errors= new HashMap<>();

            bindingResult.getFieldErrors().forEach(
                    error -> errors.put(error.getField(), error.getDefaultMessage())
            );

            String errorMsg= "";

            for(String key: errors.keySet()){
                errorMsg+= "Lỗi ở: " + key + ", lí do: " + errors.get(key) + "\n";
            }
            return errorMsg;
        }

        //TODO: Thêm code gọi xuống service layer
        log.info(request.toString());
        return "Các trường truyền vào hợp lệ!";
    }

}
