package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most once group
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        //your code here
        if (userMobile.contains(mobile.trim())) {
            throw new Exception("User already exists");
        }

        userMobile.add(mobile.trim());
        User user = new User(name.trim(),mobile.trim());
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group customGroupCount". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // If group is successfully created, return group.
        //your code here
        List<User> listOfUser = users;
        User admin = listOfUser.get(0);
        String groupName = "";
        int numberOfParticipants = listOfUser.size();

        if(listOfUser.size() == 2){//the group is a personal chat
            groupName = listOfUser.get(1).getName();
        }
        else{
            this.customGroupCount += 1;
            groupName = "Group " + customGroupCount;
        }

        Group group = new Group(groupName,numberOfParticipants);
        adminMap.put(group,admin);// add admin to the adminMap
        groupUserMap.put(group,users);// add the list of group to the group-user-Map
        return  group;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        //your code here
        messageId++;
        Message message = new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        //your code here
        if(adminMap.containsKey(group)){
            List<User> users = groupUserMap.get(group);
            Boolean userFound = false;
            for(User user: users){
                if(user.equals(sender)){
                    userFound = true;
                    break;
                }
            }
            if(userFound){
                senderMap.put(message, sender);
                if(groupUserMap.containsKey(group)){
                    if(groupMessageMap.get(group) !=null ){
                        List<Message> messages = groupMessageMap.get(group);// it was giving me null pointer exception

                        messages.add(message);
                        groupMessageMap.put(group, messages);
                        return messages.size();
                    }else{
                        List<Message> newMessage = new ArrayList<>();
                        newMessage.add(message);
                        groupMessageMap.put(group, newMessage);
                        return newMessage.size();
                    }

                }

            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".

        //your code here
        if(groupUserMap.containsKey(group)){
            if(adminMap.containsKey(group)){
                List<User> listOfUser = groupUserMap.get(group);
                if(listOfUser.contains(user)) {

                    adminMap.put(group,user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        //your code here
        Boolean flag = false;

        List<Message> messageList = new ArrayList<>();
        for (Message message : senderMap.keySet()){
            if (senderMap.get(message).equals(user)){
                messageList.add(message);
            }
        }
        for (Message message : messageList){
            if (senderMap.containsKey(message)){
                senderMap.remove(message);
            }
        }

        Group userGroup = null;

        for(Group group : groupUserMap.keySet()){

            if(adminMap.get(group).equals(user)){
                throw new Exception("Cannot remove admin");
            }
            List<User> userList = groupUserMap.get(group);
            if(userList.contains(user)){
                flag = true;
                userGroup = group;
                userList.remove(user);

            }
        }
        if (flag == false){
            throw new Exception("User not found");
        }

        List<Message> messages = groupMessageMap.get(userGroup);
        for (Message message : messageList){
            if (messages.contains(message)){
                messages.remove(message);
            }
        }

        int ans = groupUserMap.get(userGroup).size() + groupMessageMap.get(userGroup).size() + senderMap.size();
        return ans;
    }

    public String findMessage(Date start, Date end, int k) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        //your code here
        List<Message> messageList = new ArrayList<>();

        for (Message message : senderMap.keySet()){
            Date time = message.getTimestamp();
            if (start.before(time) && end.after(time)){
                messageList.add(message);
            }
        }
        if (messageList.size() < k){
            throw  new Exception("K is greater than the number of messages");
        }

        Map<Date , Message> hm = new HashMap<>();

        for (Message message : messageList){
            hm.put(message.getTimestamp(),message);
        }

        List<Date> dateList = new ArrayList<>(hm.keySet());

        Collections.sort(dateList, new sortCompare());

        Date date = dateList.get(k-1);
        String ans = hm.get(date).getContent();
        return ans;
    }
    class sortCompare implements Comparator<Date>
    {
        @Override
        // Method of this class
        public int compare(Date a, Date b)
        {
            /* Returns sorted data in Descending order */
            return b.compareTo(a);
        }
    }
}