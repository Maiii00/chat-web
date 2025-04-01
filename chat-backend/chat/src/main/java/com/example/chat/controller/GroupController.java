package com.example.chat.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.service.GroupService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    //加入群組
    @PostMapping("/{groupId}/join")
    public ResponseEntity<String> joinGroup(@PathVariable String groupId, @RequestParam String userId) {
        boolean added = groupService.addUserToGroup(groupId, userId);
        if (added) {
            return ResponseEntity.ok("User added to group successfully");
        }
        return ResponseEntity.badRequest().body("User is already in the group");
    }

    //離開群組
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(@PathVariable String groupId, @RequestParam String userId) {
        boolean removed = groupService.removeUserFromGroup(groupId, userId);
        if (removed) {
            return ResponseEntity.ok("User removed from group successfully");
        }
        return ResponseEntity.badRequest().body("User is not in the group");
    }

    //獲取成員
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Set<String>> getGroupMembers(@PathVariable String groupId) {
        Set<String> members = groupService.getGroupMembers(groupId);
        if (members == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(members);
    }
}
