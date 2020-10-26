## Development environment for writing docs

In one terminal, start sbt and type:

    docs/mdoc --watch

Assuming you have `nodejs` and `yarn` install, in another terminal, navigate
to the `website` directory:

    yarn install

    yarn start

Then, edit the documents under `src/main/markdown`. The docs are served
locally at http://localhost:3000/
