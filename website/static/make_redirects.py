files = {
  "common-protos.html": "docs/common-protos",
  "contact.html": "docs/contact",
  "customizations.html": "docs/customizations",
  "faq.html": "docs/faq",
  "generated-code.html": "docs/generated-code",
  "getting-started.html": "docs/getting-started",
  "grpc.html": "docs/grpc",
  "installation.html": "docs/installation",
  "json.html": "docs/json",
  "migrating.html": "docs/upgrading",
  "sbt-settings.html": "docs/sbt-settings",
  "scala.js.html": "docs/scala.js",
  "scalapbc.html": "docs/scalapbc",
  "sealed-oneofs.html": "docs/sealed-oneofs",
  "sparksql.html": "docs/sparksql",
  "third-party-protos.html": "docs/third-party-protos",
  "user_defined_options.html": "docs/user_defined_options",
}

tmpl = open('redir.html').read()
for k, v in files.items():
  out = open(k, 'w')
  out.write(tmpl % {'new': v})
  out.close()

  

