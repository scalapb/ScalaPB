{pkgs ? import <nixpkgs> {
  config = {
    packageOverrides = pkgs: {
      sbt = pkgs.sbt.override { jre = pkgs.openjdk11; };
    };
  };
}} :
pkgs.mkShell {
  buildInputs = [
    pkgs.sbt
    pkgs.openjdk11
    pkgs.nodejs

    # keep this line if you use bash
    pkgs.bashInteractive
  ];
}
