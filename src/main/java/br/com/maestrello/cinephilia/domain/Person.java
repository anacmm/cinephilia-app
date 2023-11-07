
package br.com.maestrello.cinephilia.domain;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Person {

   
    private Boolean adult;
    @SerializedName("also_known_as")
    private List<String> alsoKnownAs;
   
    private String biography;
   
    private String birthday;
   
    private Object deathday;
   
    private Long gender;
   
    private String homepage;
   
    private Long id;
    @SerializedName("imdb_id")
    private String imdbId;
    @SerializedName("known_for_department")
    private String knownForDepartment;
   
    private String name;
    @SerializedName("place_of_birth")
    private String placeOfBirth;
   
    private Double popularity;
    @SerializedName("profile_path")
    private String profilePath;

}
