package web.weblab4.network.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest implements Serializable {
    private String email;
    private String password;
}
