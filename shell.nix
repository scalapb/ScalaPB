{ pkgs ? import <nixpkgs> { } }:

with pkgs;

let
  sbt = pkgs.sbt.override { jre = openjdk11; };
in
mkShell {
  buildInputs = [
    sbt
    openjdk11
    nodejs
    yarn
    clang
    graalvm-ce

    # keep this line if you use bash
    bashInteractive
  ];
}
