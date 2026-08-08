package io.github.tphung1724.workforge.identity.application.port.in;

import io.github.tphung1724.workforge.identity.application.command.RegisterCommand;
import io.github.tphung1724.workforge.identity.application.result.RegisterResult;

public interface RegisterUseCase {

    RegisterResult register(RegisterCommand command);
}
