package com.jordania.api.Tutor;

import org.springframework.stereotype.Service;

import com.jordania.api.Tutor.TutorDTO.TutorRequestDTO;

@Service
public class TutorService {
    public Tutor createTutor(TutorRequestDTO data){
        Tutor newTutor = new Tutor();
        newTutor.setName(data.name());
        newTutor.setUsername(data.username());
        newTutor.setIs_private(data.is_private());
        newTutor.setBirthday(data.birthday());
        newTutor.setUpdated_at(data.updated_at());
        newTutor.setReports_counter(data.reports_counter());
        newTutor.setImg_url(data.img_url());
        
        return newTutor;
    }
}
