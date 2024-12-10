package web.weblab4.network.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetRequest implements Serializable {
    private String password;
    private String resetToken;
}
