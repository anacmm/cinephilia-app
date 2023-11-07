
package br.com.maestrello.cinephilia.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Cast {

    private Boolean adult;
    private Long castId;
    private String character;
    private String creditId;
    private Long gender;
    private Long id;
    @SerializedName("known_for_department")
    private String knownForDepartment;
    private String name;
    private Long order;
    @SerializedName("original_name")
    private String originalName;
    private Double popularity;
    @SerializedName("profile_path")
    private String profilePath;


}
