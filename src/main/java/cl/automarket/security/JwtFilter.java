package cl.automarket.security;
import cl.automarket.repo.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
@Component
public class JwtFilter extends OncePerRequestFilter {
 private final JwtService jwt;private final UserRepository users;
 public JwtFilter(JwtService jwt,UserRepository users){this.jwt=jwt;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String header=req.getHeader("Authorization");
  if(header!=null && header.startsWith("Bearer ")){
   try{String email=jwt.email(header.substring(7));users.findByEmail(email).ifPresent(u->{
    var auth=new UsernamePasswordAuthenticationToken(u.email,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.rol)));
    SecurityContextHolder.getContext().setAuthentication(auth);
   });}catch(Exception ignored){SecurityContextHolder.clearContext();}
  }
  chain.doFilter(req,res);
 }
}
