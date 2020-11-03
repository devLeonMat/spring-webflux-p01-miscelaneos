package com.rleon.springboot.reactor.app;

import com.rleon.springboot.reactor.app.models.Comments;
import com.rleon.springboot.reactor.app.models.UserComment;
import com.rleon.springboot.reactor.app.models.Usuario;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
//        iterableExample1();
//        iterableExample2();
//        iterableExample3();
//        iterableExample4();
//        iterableExample5();
//        iterableExample6();
//        Example7();
//        ExampleCommentWithZip();
//        ExampleCommentWithZipMode2();
//        ExampleWithRange();
//        intervalExample();
//        intervalExample2();
//        intervalInfiniteExample();
        intervalInfiniteFromCreate();
    }


    public void intervalInfiniteFromCreate() {

        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private Integer contador = 0;

                @Override
                public void run() {
                    emitter.next(++contador);
                    if (contador == 10) {  // finalice with count
                        timer.cancel();
                        emitter.complete();
                    }
                    if (contador == 5) {   // finalice with error
                        emitter.error(new InterruptedException("Error, process stop in flux 5"));
                    }
                }
            }, 1000, 1000);
        })
//                .doOnNext(o -> log.info(o.toString()))            // estos dos elementos los pasamos al subscribe
//                .doOnComplete(() -> log.info("Process complete"))
//                .subscribe();
                .subscribe(o -> log.info(o.toString()),
                        error -> log.error(error.getMessage()),
                        () -> log.info("Process complete"));


    }


    public void intervalInfiniteExample() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // first mode
        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(i -> {
                    if (i >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta el 5"));
                    }
                    return Flux.just(i);
                })
                .map(i -> "Hola " + i)
//                .doOnNext(log::info) // se quita pra no imprimir dos veces ya que el subcribe lo hace
                .retry(2)  // reintenta ejecuciones las veces indicadas cuando hay un error
                .subscribe(log::info, e -> log.error(e.getMessage()));

        latch.await();

        // second mode
//        Flux.interval(Duration.ofSeconds(1))
//                .doOnTerminate(() -> latch.countDown())
//                .flatMap(i -> {
//                    if (i >= 5) {
//                        return Flux.error(new InterruptedException("Solo hasta el 5"));
//                    }
//                    return Flux.just(i);
//                })
//                .map(i -> "Hola " + i)
//                .doOnNext(s -> log.info(s))
//                .retry(2)
//                .subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
//
//        latch.await();

    }


    public void intervalExample2() throws InterruptedException {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.subscribe();   // se ejecuta en segundo plano
//        rango.blockLast();   // hace que bloquee hasta que termine y es mejor

//        Thread.sleep(13000);  // hace una pausa en el hilo en ms

    }

    public void intervalExample() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retrazo = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retrazo, (ra, re) -> ra)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();

    }


    public void ExampleWithRange() {
        /*   1ra forma **/
        Flux<Integer> range = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4).map(i -> (i * 2))
                .zipWith(range, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos)).subscribe(txt -> log.info(txt));
        /* 2da forma **/
        Flux.just(1, 2, 3, 4).map(i -> (i * 2))
                .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Segundo Flux: %d, Segundo Flux: %d", uno, dos)).subscribe(log::info);

    }

    public void ExampleCommentWithZipMode2() {
        log.info("Start execution of example9");
        Mono<Usuario> userMono = Mono.fromCallable(() -> new Usuario("Jhon", "Cena"));

        Mono<Comments> commentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComent("test Comment 1");
            comments.addComent("test Comment 2");
            comments.addComent("test Comment 3");
            comments.addComent("test Comment 4");
            comments.addComent("test Comment 5");
            return comments;
        });

        Mono<UserComment> userCommentMono = userMono.zipWith(commentsMono)
                .map(tuple -> {
                    Usuario u = tuple.getT1();
                    Comments c = tuple.getT2();
                    return new UserComment(u, c);
                });

        userCommentMono.subscribe(uc -> log.info(uc.toString()));

        log.info("Start execution of example9");
    }

    public void ExampleCommentWithZip() {
        log.info("Start execution of example8");
        Mono<Usuario> userMono = Mono.fromCallable(() -> new Usuario("Jhon", "Cena"));

        Mono<Comments> commentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComent("test Comment 1");
            comments.addComent("test Comment 2");
            comments.addComent("test Comment 3");
            comments.addComent("test Comment 4");
            comments.addComent("test Comment 5");
            return comments;
        });

        // A MODE
        //userMono.zipWith(commentsMono, (usuario, comments) -> new UserComment(usuario, comments)).subscribe(uc -> log.info(uc.toString()));

        // OTHER MODE
        //userMono.zipWith(commentsMono, UserComment::new).subscribe(uc -> log.info(uc.toString()));

        //OTHER MODE
        Mono<UserComment> userCommentMono = userMono.zipWith(commentsMono, UserComment::new);
        userCommentMono.subscribe(uc -> log.info(uc.toString()));

        log.info("Start execution of example8");
    }

    public void Example7() {
        log.info("Start execution of example7");
        Mono<Usuario> userMono = Mono.fromCallable(() -> new Usuario("Jhon", "Cena"));

        Mono<Comments> commentsMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComent("test Comment 1");
            comments.addComent("test Comment 2");
            comments.addComent("test Comment 3");
            comments.addComent("test Comment 4");
            comments.addComent("test Comment 5");
            return comments;
        });

        userMono.flatMap(u -> commentsMono.map(c -> new UserComment(u, c))).subscribe(userComment -> log.info(userComment.toString()));
        log.info("End execution of example7");
    }

    public void iterableExample6() {
        log.info("Start execution of example6");
        List<Usuario> usersList = new ArrayList<>();
        usersList.add(new Usuario("Andres", "reyes"));
        usersList.add(new Usuario("pedro", "fulano"));
        usersList.add(new Usuario("Maria", "Fulano"));
        usersList.add(new Usuario("diego", "ramires"));
        usersList.add(new Usuario("juan", "vargas"));
        usersList.add(new Usuario("Bruce", "lee"));
        usersList.add(new Usuario("Bruce", "willis"));

        Flux.fromIterable(usersList)
                .collectList()
                .subscribe(list -> list.forEach(item -> log.info(item.toString())));

        log.info("End execution of example6");
    }

    public void iterableExample5() {
        log.info("Start execution of example5");
        List<Usuario> usersList = new ArrayList<>();
        usersList.add(new Usuario("Andres", "reyes"));
        usersList.add(new Usuario("pedro", "fulano"));
        usersList.add(new Usuario("Maria", "Fulano"));
        usersList.add(new Usuario("diego", "ramires"));
        usersList.add(new Usuario("juan", "vargas"));
        usersList.add(new Usuario("Bruce", "lee"));
        usersList.add(new Usuario("Bruce", "willis"));

        Flux.fromIterable(usersList)
                .map(user -> user.getNombre().toUpperCase().concat(" ").concat(user.getApellido().toUpperCase()))
                .flatMap(name -> {
                    if (name.contains("bruce".toUpperCase())) {
                        return Mono.just(name);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(String::toLowerCase).subscribe(log::info);
        log.info("End execution of example5");
    }

    public void iterableExample4() {
        log.info("Start execution of example4");
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres reyes");
        usuariosList.add("pedro fulano");
        usuariosList.add("Maria Fulano");
        usuariosList.add("diego ramires");
        usuariosList.add("juan vargas");
        usuariosList.add("Bruce lee");
        usuariosList.add("Bruce willis");

        Flux.fromIterable(usuariosList)
                .map(name -> new Usuario(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.getNombre().equalsIgnoreCase("bruce")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(user -> {
                    String name = user.getNombre().toLowerCase();
                    user.setNombre(name);
                    return user;
                }).subscribe(e -> log.info(e.toString()));
        log.info("End execution of example4");
    }


    public void iterableExample3() {
        log.info("Start execution of example3");
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres reyes");
        usuariosList.add("pedro fulano");
        usuariosList.add("Maria Fulano");
        usuariosList.add("diego ramires");
        usuariosList.add("juan vargas");
        usuariosList.add("Bruce lee");
        usuariosList.add("Bruce willis");

        Flux<String> names = Flux.fromIterable(usuariosList);

        Flux<Usuario> usuarios = names.map(name -> new Usuario(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("Bruce"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                }).map(user -> {
                    String name = user.getNombre().toLowerCase();
                    user.setNombre(name);
                    return user;
                });

        usuarios.subscribe(e -> log.info(e.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Ha finalizado la ejecucion del observable con exito!")
        );
        log.info("End execution of example3");
    }

    public void iterableExample2() {
        log.info("Start execution of example2");
        Flux<String> names = Flux.just("Andres reyes", "pedro fulano", "Maria Fulano", "diego ramires", "juan vargas", "Bruce lee", "Bruce willis");

        Flux<Usuario> usuarios = names.map(name -> new Usuario(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("Bruce"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                }).map(user -> {
                    String name = user.getNombre().toLowerCase();
                    user.setNombre(name);
                    return user;
                });

//        names.subscribe(e -> log.info(e.toString()),
        usuarios.subscribe(e -> log.info(e.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Ha finalizado la ejecucion del observable con exito!")
        );
        log.info("End execution of example2");
    }

    public void iterableExample1() {
        log.info("Start execution of example1");

        Flux<Usuario> names = Flux.just("Andres reyes", "pedro fulano", "Maria Fulano", "diego ramires", "juan vargas", "Bruce lee", "Bruce willis").
                map(name -> new Usuario(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("Bruce"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                }).map(user -> {
                    String name = user.getNombre().toLowerCase();
                    user.setNombre(name);
                    return user;
                });

        names.subscribe(e -> log.info(e.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Ha finalizado la ejecucion del observable con exito!")
        );

        log.info("End execution of example1");
    }
}
