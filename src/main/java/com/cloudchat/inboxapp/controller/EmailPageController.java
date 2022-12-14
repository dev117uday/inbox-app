package com.cloudchat.inboxapp.controller;

import com.cloudchat.inboxapp.models.Email;
import com.cloudchat.inboxapp.models.Folder;
import com.cloudchat.inboxapp.service.EmailService;
import com.cloudchat.inboxapp.service.FoldersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class EmailPageController {

    @Autowired
    private FoldersService foldersService;
    @Autowired
    private EmailService emailService;

    @GetMapping(value = "/email/{id}")
    public String getEmailPage(@PathVariable String id, @RequestParam String folder,
                               @AuthenticationPrincipal OidcUser principal,
                               @RequestParam(value = "folder") String currentfolder,
                               Model model) {

        if (principal != null) {

            String loginId = principal.getEmail();
            model.addAttribute("profile", principal.getClaims());

            List<Folder> initFolders = foldersService.init(loginId);
            model.addAttribute("defaultFolders", initFolders);

            try {
                UUID uuid = UUID.fromString(id);
                Email email = emailService.GetById(uuid);
                String toIds = String.join(",", email.getTo());

                model.addAttribute("email", email);
                model.addAttribute("toIds", toIds);

                emailService.markEmailRead(loginId, email, folder);

                Map<String, Integer> folderToUnreadCounts = foldersService.getUnreadCountsMap(loginId);
                model.addAttribute("folderToUnreadCounts", folderToUnreadCounts);

                model.addAttribute("current", currentfolder);

                return "email-page";
            } catch (IllegalArgumentException e) {
                return "inbox-page";
            }
        }
        return "index";
    }

    @GetMapping(value = "/moveTo")
    public ModelAndView moveToFolderController(
            @AuthenticationPrincipal OidcUser principal,
            @RequestParam(value = "to", required = false) String toFolder,
            @RequestParam(value = "emailId", required = false) String emailId,
            @RequestParam(value = "from", required = false) String fromEmail,
            @RequestParam(value = "fc", required = false) String currentFolder) {

        if (principal != null) {
            emailService.moveToFolder(toFolder, UUID.fromString(emailId), fromEmail, currentFolder);
        }

        return new ModelAndView("redirect:/");
    }

    @GetMapping(value = "/delete")
    public ModelAndView deleteMessageController(
            @AuthenticationPrincipal OidcUser principal,
            @RequestParam(value = "emailId", required = false) String emailId,
            @RequestParam(value = "from", required = false) String fromEmail,
            @RequestParam(value = "fc", required = false) String currentFolder) {

        if (principal != null) {
            emailService.deleteMessage(UUID.fromString(emailId), fromEmail, currentFolder);
        }

        return new ModelAndView("redirect:/");
    }

}

