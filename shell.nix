{
  pkgs ? import <nixpkgs> { },
}:

with pkgs;

let
  sbt = pkgs.sbt.override { jre = openjdk17; };
in
mkShell {
  buildInputs = [
    sbt
    coursier
    metals
    openjdk17
    nodejs
    yarn
    clang
    graalvmPackages.graalvm-ce

    # keep this line if you use bash
    bashInteractive
  ];
}
