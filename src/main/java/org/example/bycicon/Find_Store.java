package org.example.bycicon;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import store_Template.RetailerData;

import java.sql.SQLException;

@Controller
@RequestMapping("/retailer")
public class Find_Store {

    @GetMapping("/{slug}")
    public String Retailer(@PathVariable String slug, Model model) throws SQLException {
        RetailerData retailer = new RetailerData(slug);
        if (retailer.getRetailerId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Retailer not found");
        }
        model.addAttribute("retailer", retailer);
        return "store";
    }
}
