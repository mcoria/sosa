package net.chesstango.sosa.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class DemoPayload {
    private String id;
    private String content;
}
