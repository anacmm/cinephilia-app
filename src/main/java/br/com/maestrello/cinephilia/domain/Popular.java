
package br.com.maestrello.cinephilia.domain;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Popular {

    private Long page;
    private List<Movie> results;
    @SerializedName("total_pages")
    private Long totalPages;
    @SerializedName("total_results")
    private Long totalResults;


}
