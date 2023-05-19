/*
 *  Copyright (c) 2023, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.common.command.subcommand;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@UtilityClass
public class SubCommandManager {
    @Getter
    private final Collection<SubCommand> subCommands = new ArrayList<>();

    public void register(final SubCommand... commands) {
        subCommands.addAll(Arrays.asList(commands));
    }

    public void unregister(final SubCommand... commands) {
        subCommands.removeAll(Arrays.asList(commands));
    }
}
