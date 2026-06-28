package com.example.carsharingapp.dto;

import lombok.Data;

@Data
public class RentalResponseDto {
   private Long id;
   private String rentalDate;
   private String actualReturnDate;
   private String returnDate;
   private Long carId;
   private Long userId;
}
