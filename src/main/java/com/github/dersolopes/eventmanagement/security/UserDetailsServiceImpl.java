package com.github.dersolopes.eventmanagement.security;

import com.github.dersolopes.eventmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação do UserDetailsService para carregar usuários do banco de dados.
 * 
 * Esta classe resolve o problema de StackOverflowError ao fornecer uma implementação
 * explícita do UserDetailsService que o Spring Security usa durante a autenticação.
 * O AuthenticationManager precisa deste serviço para carregar os dados do usuário
 * (incluindo senha e authorities) para verificar as credenciais fornecidas no login.
 * 
 * Ao buscar o usuário diretamente no UserRepository, evitamos dependências cíclicas
 * que causavam o erro de estouro de pilha.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carrega os detalhes do usuário pelo e-mail (que é usado como username).
     * 
     * @param email O e-mail do usuário a ser carregado
     * @return UserDetails com os dados do usuário (senha, authorities, etc.)
     * @throws UsernameNotFoundException se o usuário não for encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }
}
