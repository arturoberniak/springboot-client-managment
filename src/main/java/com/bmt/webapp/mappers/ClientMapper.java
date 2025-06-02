package com.bmt.webapp.mappers;

import com.bmt.webapp.models.Client;
import com.bmt.webapp.models.ClientDto;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ClientMapper {

    public ClientDto mapToClientDto(Client client){
        ClientDto clientDto = new ClientDto();
        clientDto.setFirstName(client.getFirstName());
        clientDto.setLastName(client.getLastName());
        clientDto.setEmail(client.getEmail());
        clientDto.setPhone(client.getPhone());
        clientDto.setAddress(client.getAddress());
        clientDto.setStatus(client.getStatus());
        return clientDto;
    }

    public Client mapToClient(ClientDto clientDto){
        Client client = new Client();
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setEmail(clientDto.getEmail());
        client.setPhone(clientDto.getPhone());
        client.setAddress(clientDto.getAddress());
        client.setStatus(clientDto.getStatus());
        client.setCreatedAt(new Date());
        return client;
    }

    public void updateClientFromDto(ClientDto clientDto, Client client) {
        client.setFirstName(clientDto.getFirstName());
        client.setLastName(clientDto.getLastName());
        client.setEmail(clientDto.getEmail());
        client.setPhone(clientDto.getPhone());
        client.setAddress(clientDto.getAddress());
        client.setStatus(clientDto.getStatus());
    }
}
