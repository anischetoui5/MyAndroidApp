package com.budgetpal.budgetppaal.api;

import com.budgetpal.budgetppaal.models.TransactionResponse;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Field;

public interface ApiService {

    // ---------------------------------------------------
    // 1️⃣ SAVE USER AFTER GOOGLE LOGIN
    //     (This matches your saveUser.php)
    // ---------------------------------------------------
    @FormUrlEncoded
    @POST("saveUser.php")
    Call<TransactionResponse> saveUser(
            @Field("firebase_token") String token
    );

    // ---------------------------------------------------
    // 2️⃣ ADD TRANSACTION (your original API)
    // ---------------------------------------------------
    @FormUrlEncoded
    @POST("addTransaction.php")
    Call<TransactionResponse> addTransaction(
            @Field("userID") int userID,
            @Field("categoryID") int categoryID,
            @Field("amount") double amount,
            @Field("date") String date,
            @Field("description") String description
    );
}
