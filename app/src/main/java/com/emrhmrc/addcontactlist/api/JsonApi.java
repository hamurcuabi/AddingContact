package com.emrhmrc.addcontactlist.api;


import com.emrhmrc.addcontactlist.models.Number;
import com.emrhmrc.addcontactlist.models.RangeModel;
import com.emrhmrc.addcontactlist.models.WhatsAppNumber;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface JsonApi {

    @POST("Numbers/GetNumbersByRange")
    Call<List<Number>> getNumbersByRange(@Body RangeModel rangeModel);

    @POST("WhatsAppNumbers/PostWhatsAppNumber")
    Call<WhatsAppNumber> postWhatsAppNumber(@Body WhatsAppNumber model);
    @POST("WhatsAppNumbers/PostWhatsAppNumberList")
    Call<Void> postWhatsAppNumberList(@Body List<WhatsAppNumber> model);


}
