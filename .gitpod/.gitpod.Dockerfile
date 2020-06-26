FROM gitpod/workspace-full

RUN sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.1.4/2.13-2.1.4) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm'

RUN brew install coursier/formulas/coursier sbt
