package br.tasks.config;

import br.tasks.domain.Tarefa;
import br.tasks.dto.tarefa.TarefaRequestDTO;
import br.tasks.dto.tarefa.TarefaResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do ModelMapper utilizado na camada de serviço para converter
 * DTOs e entidades de tarefa.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Cria o ModelMapper com os mapeamentos explícitos de tarefa.
     *
     * <p>O mapeamento de entrada monta a entidade sem proprietário; o serviço
     * associa o usuário autenticado logo após a conversão. O mapeamento de
     * saída delega para {@link TarefaResponseDTO#from(Tarefa)}, que também
     * converte o usuário proprietário.</p>
     *
     * @return ModelMapper configurado
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);

        mapper.createTypeMap(TarefaRequestDTO.class, Tarefa.class)
                .setConverter(context -> {
                    TarefaRequestDTO origem = context.getSource();
                    return new Tarefa(
                            origem.descricao().trim(),
                            origem.prioridade(),
                            Boolean.TRUE.equals(origem.concluida()),
                            null
                    );
                });

        mapper.createTypeMap(Tarefa.class, TarefaResponseDTO.class)
                .setConverter(context -> TarefaResponseDTO.from(context.getSource()));

        return mapper;
    }
}
