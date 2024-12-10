package web.weblab4.network.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointRequest implements Serializable {
    private float x;
    private float y;
    private float r;
}
