------------------------------------------------------------
-- ENUM-uri
------------------------------------------------------------
CREATE TYPE tip_user AS ENUM ('PERSOANA', 'RATA');
CREATE TYPE tip_rata AS ENUM ('FLYING', 'SWIMMING', 'FLYING_AND_SWIMMING');
CREATE TYPE tip_card AS ENUM ('FLYINGCARD', 'SWIMMINGCARD');

------------------------------------------------------------
-- TABEL CARDURI
------------------------------------------------------------
CREATE TABLE public.carduri (
                                id BIGINT PRIMARY KEY,
                                tip tip_card NOT NULL,
                                nume VARCHAR(50) NOT NULL
);

------------------------------------------------------------
-- INSERĂRI CARDURI
------------------------------------------------------------
INSERT INTO public.carduri (tip, id, nume) VALUES
                                               ('FLYINGCARD',   1, 'SKYFLYERS'),
                                               ('FLYINGCARD',   2, 'AIRMASTERS'),
                                               ('FLYINGCARD',   3, 'HIGHWINGS'),
                                               ('SWIMMINGCARD', 4, 'SWIMMASTERS'),
                                               ('SWIMMINGCARD', 5, 'DUCKDIVERS'),
                                               ('FLYINGCARD',  11, 'SKYMASTERS');

------------------------------------------------------------
-- TABEL UNIFICAT USERI
------------------------------------------------------------
CREATE TABLE public.useri (
                              id BIGINT PRIMARY KEY,
                              tip tip_user NOT NULL,

                              username VARCHAR(50) NOT NULL,
                              email VARCHAR(100) NOT NULL UNIQUE,
                              password VARCHAR(50) NOT NULL,

    -- câmpuri persoană
                              nume VARCHAR(50),
                              prenume VARCHAR(50),
                              data_nasterii DATE,
                              ocupatie VARCHAR(50),

    -- câmpuri rată
                              rezistenta DOUBLE PRECISION,
                              viteza DOUBLE PRECISION,
                              tip_rata tip_rata,
                              id_card BIGINT REFERENCES public.carduri(id) ON DELETE SET NULL
);

------------------------------------------------------------
-- INSERĂRI USERI: PERSOANE
------------------------------------------------------------
INSERT INTO public.useri (tip, id, username, email, password, nume, prenume, data_nasterii, ocupatie) VALUES
                                                                                                          ('PERSOANA', 33, 'MARIAC',  'MARIA@YAHOO.COM', 'VfcpIp2w6HwlvQxFXISpVg==',     'CRISTEA',    'MARIA', '2001-11-21', 'PROFESOR'),
                                                                                                          ('PERSOANA', 11, 'ALINAP',  'ALINA@GMAIL.COM', '+U3QUfYSk5/dn8bIzo1cJw==',  'POP',        'ALINA', '2003-05-17', 'STUDENT'),
                                                                                                          ('PERSOANA', 75, 'MARIA',   'MARIA@GMAIL.COM', '61F+M5Mtw75D8CtIduadUw==', 'POP',        'MARIA', '2005-09-15', 'STUDENT'),
                                                                                                          ('PERSOANA', 44, 'DANV',    'DANV@GMAIL.COM',  'kboTsjRuivCO5ezcwOIOmw==',    'VASILESCU',  'DAN',   '1995-02-07', 'DOCTOR'),
                                                                                                          ('PERSOANA', 55, 'ELENAA',  'ELENA@YAHOO.COM', 'l70+3qyoUmjNu0CWM20hhw==',  'ANTONESCU',  'ELENA', '2000-04-05', 'ARHITECT');

------------------------------------------------------------
-- INSERĂRI USERI: RAȚE
------------------------------------------------------------
INSERT INTO public.useri (tip, id, username, email, password, rezistenta, viteza, tip_rata, id_card) VALUES
                                                                                                         ('RATA', 1,  'SKYRIDER',     'SKY@MAIL.COM',     'cuXWhsv+nXg459VsKTRbPg==', 5.0, 9.0, 'FLYING',               1),
                                                                                                         ('RATA', 2,  'RATA_NEAGRA',  'NEAGRA@RATE.COM',  'abl4KNqIYbG4y819u47+JA==', 3.8, 2.2, 'FLYING_AND_SWIMMING', NULL),
                                                                                                         ('RATA', 4,  'RATA_GALBUIE', 'GALBUIE@RATE.COM', 'xNl38inyND2AdgorRF9rlQ==', 1.9, 3.8, 'FLYING_AND_SWIMMING', 2),
                                                                                                         ('RATA', 50, 'AQUADUCK',     'AQUA@MAIL.COM',    '8/idXrZm+5kY52SiSPnoCA==', 9.0, 5.5, 'SWIMMING',           5);

------------------------------------------------------------
-- TABEL PRIETENII
------------------------------------------------------------
CREATE TABLE public.prietenii (
                                  id1 BIGINT NOT NULL,
                                  id2 BIGINT NOT NULL,
                                  PRIMARY KEY (id1, id2),
                                  FOREIGN KEY (id1) REFERENCES public.useri(id) ON DELETE CASCADE,
                                  FOREIGN KEY (id2) REFERENCES public.useri(id) ON DELETE CASCADE
);

------------------------------------------------------------
-- INSERARE PRIETENII
------------------------------------------------------------
INSERT INTO public.prietenii (id1, id2) VALUES
    (4, 44);


DROP TABLE IF EXISTS public.evenimente CASCADE;

CREATE TABLE public.evenimente (
                                   id BIGINT PRIMARY KEY,
                                   nume TEXT NOT NULL,
                                   distante TEXT NOT NULL,
                                   status VARCHAR(20) DEFAULT 'OPEN',
                                   rezultat_final TEXT,
                                   id_creator BIGINT NOT NULL, -- Coloana nouă
                                   FOREIGN KEY (id_creator) REFERENCES public.useri(id) ON DELETE CASCADE
);

INSERT INTO public.evenimente (id, nume, distante, status, rezultat_final, id_creator) VALUES
                                                                                           (1, 'DuckRace',       '50.0,100.0,150.0',  'OPEN',     NULL, 33), -- Creat de MariaC
                                                                                           (2, 'FastSwim',       '75.0,125.0',        'FINISHED', 'Timp oficial cursă: 18.750 secunde\n------------------------------------------------\nCuloar 1 (75.0m): Rata #55 -> 18.750s\nCuloar 2 (125.0m): Neocupat\n', 44), -- Creat de DanV
                                                                                           (3, 'SkyRace',        '100.0,200.0',       'OPEN',     NULL, 33), -- Creat de MariaC
                                                                                           (4, 'WaterChallenge', '60.0,120.0',        'FINISHED', 'Timp oficial cursă: 30.000 secunde\n------------------------------------------------\nCuloar 1 (60.0m): Rata #44 -> 15.000s\nCuloar 2 (120.0m): Rata #55 -> 30.000s\n', 55), -- Creat de ElenaA
                                                                                           (5, 'MixedRace',      '30.0,80.0,110.0',   'OPEN',     NULL, 11), -- Creat de AlinaP
                                                                                           (6, 'BeginnerRace',   '25.0,50.0',         'OPEN',     NULL, 44), -- Creat de DanV
                                                                                           (7, 'EliteFlight',    '150.0,200.0,250.0', 'OPEN',     NULL, 33); -- Creat de MariaC

CREATE TABLE public.event_observers (
                                        event_id BIGINT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        PRIMARY KEY (event_id, user_id),

                                        FOREIGN KEY (event_id) REFERENCES public.evenimente(id) ON DELETE CASCADE,
                                        FOREIGN KEY (user_id) REFERENCES public.useri(id) ON DELETE CASCADE
);


-- 2. Tabel nou pentru participanți (rațele care concurează efectiv)
CREATE TABLE public.event_participants (
                                           event_id BIGINT NOT NULL,
                                           user_id BIGINT NOT NULL,
                                           PRIMARY KEY (event_id, user_id),
                                           FOREIGN KEY (event_id) REFERENCES public.evenimente(id) ON DELETE CASCADE,
                                           FOREIGN KEY (user_id) REFERENCES public.useri(id) ON DELETE CASCADE
);

-- DuckRace
INSERT INTO public.event_observers VALUES (1, 4);
-- FastSwim
INSERT INTO public.event_observers VALUES (2, 55), (2, 75);
-- SkyRace
INSERT INTO public.event_observers VALUES (3, 11);
-- WaterChallenge
INSERT INTO public.event_observers VALUES (4, 44), (4, 55);
-- MixedRace
INSERT INTO public.event_observers VALUES (5, 33), (5, 75);
-- BeginnerRace
INSERT INTO public.event_observers VALUES (6, 11);
-- EliteFlight
INSERT INTO public.event_observers VALUES (7, 33);

CREATE TABLE public.mesaje (
                               id BIGINT PRIMARY KEY,
                               from_user_id BIGINT NOT NULL,
                               to_user_id BIGINT NOT NULL,
                               text TEXT NOT NULL,
                               data TIMESTAMP NOT NULL,
                               status VARCHAR(20) DEFAULT 'UNREAD',
                               reply_to_id BIGINT,
                               FOREIGN KEY (from_user_id) REFERENCES public.useri(id) ON DELETE CASCADE,
                               FOREIGN KEY (to_user_id) REFERENCES public.useri(id) ON DELETE CASCADE,
                               FOREIGN KEY (reply_to_id) REFERENCES public.mesaje(id) ON DELETE SET NULL
);

------------------------------------------------------------
-- TABEL CERERI PRIETENIE
------------------------------------------------------------
-- Statusul va fi stocat ca string: 'PENDING', 'APPROVED', 'REJECTED'

DROP TABLE IF EXISTS public.cereri_prietenie;

CREATE TABLE public.cereri_prietenie (
                                         id BIGINT PRIMARY KEY, -- ID unic (milisecunde)
                                         id_expeditor BIGINT NOT NULL,
                                         id_destinatar BIGINT NOT NULL,
                                         status VARCHAR(20) NOT NULL, -- 'PENDING', 'APPROVED', 'REJECTED'
                                         data TIMESTAMP NOT NULL,

                                         FOREIGN KEY (id_expeditor) REFERENCES public.useri(id) ON DELETE CASCADE,
                                         FOREIGN KEY (id_destinatar) REFERENCES public.useri(id) ON DELETE CASCADE
);


------------------------------------------------------------
-- VERIFICARE
------------------------------------------------------------
SELECT * FROM public.carduri;
SELECT * FROM public.useri;
SELECT * FROM public.prietenii;
SELECT * FROM public.evenimente;
SELECT * FROM public.event_observers;
SELECT * FROM public.mesaje;
SELECT * FROM public.cereri_prietenie;
SELECT * FROM public.event_participants;
