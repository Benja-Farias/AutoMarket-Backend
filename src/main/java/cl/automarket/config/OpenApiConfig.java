package cl.automarket.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
 @Bean OpenAPI api(){return new OpenAPI().info(new Info().title("AutoMarket API").version("1.0")
  .description("Inventario, cuentas y consultas de AutoMarket"))
  .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
  .schemaRequirement("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));}
}
