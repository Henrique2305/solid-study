package cotuba.md;

import cotuba.domain.Capitulo;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class RenderizadorMDParaHTML {

    public List<Capitulo> renderiza(Path diretorioDosMD) {
        return obtemArquivosMD(diretorioDosMD).stream()
                .map(arquivoMD -> {
                    Capitulo capitulo = new Capitulo();
                    Node documento = parseDoMD(arquivoMD, capitulo);
                    renderizaParaHTML(arquivoMD, capitulo, documento);
                    return capitulo;
                }).toList();
    }

    private List<Path> obtemArquivosMD(Path diretorioDosMD) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.md");
        try (Stream<Path> arquivosMD = Files.list(diretorioDosMD)) {
            return arquivosMD
                    .filter(matcher::matches)
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Node parseDoMD(Path arquivoMD, Capitulo capitulo) {
        Parser parser = Parser.builder().build();
        Node document = null;
        try {
            document = parser.parseReader(Files.newBufferedReader(arquivoMD));

            // TODO: Extrair essa classe anônima para uma classe própria
            document.accept(new AbstractVisitor() {
                @Override
                public void visit(Heading heading) {
                    if (heading.getLevel() == 1) {
                        // capítulo
                        String tituloDoCapitulo =
                                ((Text) heading.getFirstChild()).getLiteral();

                        capitulo.setTitulo(tituloDoCapitulo);

                    } else if (heading.getLevel() == 2) {
                        // seção
                    } else if (heading.getLevel() == 3) {
                        // título
                    }
                }

            });
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao fazer parse do arquivo " + arquivoMD, ex);
        }

        return document;
    }

    private void renderizaParaHTML (Path arquivoMD, Capitulo capitulo, Node document){
        try {
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String html = renderer.render(document);

            capitulo.setConteudoHTML(html);
        } catch (Exception ex) {
            throw new IllegalStateException("Erro ao renderizar para HTML o arquivo " + arquivoMD, ex);
        }
    }
}
