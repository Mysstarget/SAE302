

# SAE302 — Méthodes du protocole client/serveur

## Principe général

Le protocole fonctionne avec des échanges simples en CSV.

Le client envoie une commande au serveur :

```txt
Commande,parametre1,parametre2,parametre3
```

Le serveur traite la demande, vérifie les données, modifie si besoin la base de données, puis renvoie une réponse au client :

```txt
Code,Type,Message
```

Exemple :

```txt
201,CREATE_USER,Utilisateur créé
```

ou

```txt
404,CONNECT,Utilisateur inexistant
```

---

## Codes utilisés

| Code  | Signification                              |
| ----- | ------------------------------------------ |
| `200` | Succès                                     |
| `201` | Création réussie                           |
| `400` | Requête invalide                           |
| `401` | Mauvais mot de passe ou accès refusé       |
| `402` | Erreur lors de la récupération des données |
| `404` | Ressource inexistante                      |
| `405` | Erreur serveur / erreur base de données    |
| `409` | Conflit, ressource déjà existante          |
| `410` | Limite user 4 max                          |

---

## Gestion des mises à jour sans table `update`

Pour éviter d’utiliser une table `update`, les informations à envoyer au client sont retrouvées directement grâce à des attributs dans les tables existantes.

Exemples d’attributs possibles :

### Table `User`

| Attribut        | Utilité                                                                           |
| --------------- | --------------------------------------------------------------------------------- |
| `U_Id_user`       | Identifiant de l’utilisateur                                                      |
| `username`      | Nom d’utilisateur                                                                 |
| `password` | Mot de passe                                                               |

### Table `Friend`

| Attribut   | Utilité                                    |
| ---------- | ------------------------------------------ |
| `F_src_user` | Utilisateur qui envoie la demande          |
| `F_dst_user` | Utilisateur qui reçoit la demande          |
| `status`   | `PENDING`, `ACCEPTED`, `REFUSED`           |
| `seen_src` | Indique si l’émetteur a vu la réponse      |
| `seen_dst` | Indique si le destinataire a vu la demande |

### Table `Message`

| Attribut    | Utilité                                                 |
| ----------- | ------------------------------------------------------- |
| `id_msg`    | Identifiant du message                                  |
| `src_user`  | Utilisateur qui envoie le message                       |
| `dst_user`  | Destinataire si message privé                           |
| `dst_group` | Groupe si message de groupe                             |
| `type`      | `PRIVATE` ou `GROUP`                                    |
| `content`   | Contenu du message                                      |
| `delivered` | Indique si le message privé a déjà été envoyé au client |
| `read`      | Indique si le message a été lu                          |

### Table `Group`

| Attribut     | Utilité               |
| ------------ | --------------------- |
| `id_group`   | Identifiant du groupe |
| `group_name` | Nom du groupe         |
| `owner`      | Créateur du groupe    |

### Table `Group_Member`

| Attribut           | Utilité                                          |
| ------------------ | ------------------------------------------------ |
| `id_group_member`         | Groupe concerné                                  |
| `id_group_user`          | Membre du groupe                                 |
| `role`             | `OWNER`, `ADMIN`, `MEMBER`                       |
| `seen_join`        | Indique si l’utilisateur a vu qu’il a été ajouté |
| `last_seen_msg_id` | Dernier message de groupe reçu par l’utilisateur |

L’appel `Update` du client permet donc au serveur de chercher directement :

* les messages privés avec `delivered = 0`
* les demandes d’amis avec `status = PENDING` et `seen_dst = 0`
* les réponses aux demandes d’amis avec `seen_src = 0`
* les ajouts dans des groupes avec `seen_join = 0`
* les messages de groupe avec `id_msg > last_seen_msg_id`

---

# Méthodes du protocole

---

## Création utilisateur

### Commande

```txt
Create,User,password
```

### Échange client / serveur

Client demande au serveur de créer un utilisateur.


Le serveur vérifie d’abord que le nom d’utilisateur ne contient pas de caractères spéciaux interdits.

Si le nom contient des caractères spéciaux :

```txt
Serveur -> Client : 400,CREATE_USER,Nom d'utilisateur invalide
```

Si le nom est valide, le serveur vérifie si l’utilisateur existe déjà.

Si l’utilisateur existe déjà :

```txt
Serveur -> Client : 409,CREATE_USER,Utilisateur déjà existant
```

Si l’utilisateur n’existe pas, le serveur crée l’utilisateur dans la table `User`.

Si la création échoue :

```txt
Serveur -> Client : 405,CREATE_USER,Erreur lors de la création
```

Si la création réussit :

```txt
Serveur -> Client : 201,CREATE_USER,Utilisateur créé
```

Le client reçoit le code et affiche un message adapté à l’utilisateur.

---

## Connexion utilisateur

### Commande

```txt
Connect,User,password
```

### Échange client / serveur

Client demande à se connecter.


Le serveur vérifie que l’utilisateur existe dans la table `User`.

Si l’utilisateur n’existe pas :

```txt
Serveur -> Client : 404,CONNECT,Utilisateur inexistant
```

Si l’utilisateur existe, le serveur vérifie le mot de passe.

Si le mot de passe est incorrect :

```txt
Serveur -> Client : 401,CONNECT,Mot de passe incorrect
```

Si le mot de passe est correct, le serveur récupère les informations nécessaires :

* liste des amis
* liste des groupes
* messages privés non livrés
* demandes d’amis en attente
* messages de groupe non reçus

Si la récupération des données échoue :

```txt
Serveur -> Client : 402,CONNECT,Erreur récupération données
```

Si tout est correct :

```txt
Serveur -> Client : 200,CONNECT,OK,Token;FRIENDS=...;GROUPS=...;MSG=...
```

exemple:
```
200,CONNECT,OK,Toket;FRIENDS=Pierre,Paul
```

Après l’envoi des messages privés au client, le serveur peut passer leurs attributs `delivered` à `1`.

Le client traite ensuite les données reçues et affiche l’interface utilisateur.

---

## Suppression utilisateur

### Commande

```txt
Delete,User,password
```

### Échange client / serveur

Client demande la suppression de son compte.

```txt
Client -> Serveur : Delete,User,password
```

Le serveur vérifie que l’utilisateur existe.

Si l’utilisateur n’existe pas :

```txt
Serveur -> Client : 404,DELETE_USER,Utilisateur inexistant
```

Si l’utilisateur existe, le serveur vérifie le mot de passe.

Si le mot de passe est incorrect :

```txt
Serveur -> Client : 401,DELETE_USER,Mot de passe incorrect
```

Si le mot de passe est correct, le serveur désactive ou supprime l’utilisateur.

Solution conseillée : ne pas supprimer directement l’utilisateur, mais passer l’attribut `is_deleted` à `1`.

```txt
User.is_deleted = 1
```

Cela évite de casser les anciennes conversations, les messages ou les relations existantes.

Le serveur peut ensuite supprimer ou désactiver les relations d’amis et les appartenances aux groupes.

Si l’opération échoue :

```txt
Serveur -> Client : 405,DELETE_USER,Erreur suppression utilisateur
```

Si l’opération réussit :

```txt
Serveur -> Client : 200,DELETE_USER,Utilisateur supprimé
```

Les autres clients verront la modification lors de leur prochain appel `Update`, car le serveur renverra une liste d’amis et de groupes mise à jour.

---

## Mise à jour client

### Commande

```txt
Update,User
```

### Échange client / serveur

Le client demande au serveur s’il y a de nouvelles données pour l’utilisateur.

Le serveur vérifie que l’utilisateur existe.

Si l’utilisateur n’existe pas :

```txt
Serveur -> Client : 404,UPDATE,Utilisateur inexistant
```

Si l’utilisateur existe, le serveur cherche directement dans les tables existantes.

Il cherche les nouveaux messages privés :

```txt
Message.dst_user = User
Message.type = PRIVATE
Message.delivered = 0
```

Il cherche les demandes d’amis non vues :

```txt
Friend.dst_user = User
Friend.status = PENDING
Friend.seen_dst = 0
```

Il cherche les réponses aux demandes d’amis :

```txt
Friend.src_user = User
Friend.status = ACCEPTED ou REFUSED
Friend.seen_src = 0
```

Il cherche les groupes où l’utilisateur vient d’être ajouté :

```txt
Group_Member.id_user = User
Group_Member.seen_join = 0
```

Il cherche les messages de groupe non reçus :

```txt
Message.type = GROUP
Message.id_msg > Group_Member.last_seen_msg_id
```

Si aucune donnée n’est disponible :

```txt
Serveur -> Client : 200,UPDATE,NO_DATA
```

Si des données sont disponibles :

```txt
Serveur -> Client : 200,UPDATE,DATA;MSG=...;FRIEND_REQUEST=...;GROUP_MSG=...
```

Après l’envoi, le serveur modifie les attributs concernés.

Exemples :

```txt
Message.delivered = 1
Friend.seen_dst = 1
Friend.seen_src = 1
Group_Member.seen_join = 1
Group_Member.last_seen_msg_id = dernier message reçu
```

Le client traite les données et met à jour son affichage.

---

## Envoi de message privé

### Commande

```txt
Send_Msg,Src_User,Dst_User,Msg
```

### Échange client / serveur

Client envoie un message privé à un autre utilisateur.

```txt
Client -> Serveur : Send_Msg,User,luc,Salut
```

Le serveur vérifie que `Src_User` existe.

Si l’utilisateur source n’existe pas :

```txt
Serveur -> Client : 404,SEND_MSG,Utilisateur source inexistant
```

Le serveur vérifie que `Dst_User` existe.

Si le destinataire n’existe pas :

```txt
Serveur -> Client : 404,SEND_MSG,Destinataire inexistant
```

Le serveur vérifie que le message n’est pas vide.

Si le message est vide ou invalide :

```txt
Serveur -> Client : 400,SEND_MSG,Message invalide
```

Le serveur stocke le message dans la table `Message`.

Exemple d’insertion logique :

```txt
src_user = User
dst_user = luc
type = PRIVATE
content = Salut
delivered = 0
read = 0
```

Si le stockage échoue :

```txt
Serveur -> Client : 405,SEND_MSG,Erreur stockage message
```

Si le stockage réussit :

```txt
Serveur -> Client : 200,SEND_MSG,Message envoyé
```

Le destinataire recevra le message lors de son prochain appel `Update`, car le message aura encore `delivered = 0`.

---

## Demande d’ami

### Commande

```txt
F_add,Src_User,Dst_User
```

### Échange client / serveur

Client demande l’ajout d’un ami.

```txt
Client -> Serveur : F_add,User,luc
```

Le serveur vérifie que `Src_User` existe.

Si l’utilisateur source n’existe pas :

```txt
Serveur -> Client : 404,F_ADD,Utilisateur source inexistant
```

Le serveur vérifie que `Dst_User` existe.

Si l’utilisateur destination n’existe pas :

```txt
Serveur -> Client : 404,F_ADD,Utilisateur destination inexistant
```

Le serveur vérifie que l’utilisateur ne s’ajoute pas lui-même.

Si `Src_User` et `Dst_User` sont identiques :

```txt
Serveur -> Client : 400,F_ADD,Impossible de s'ajouter soi-même
```

Le serveur vérifie si une relation existe déjà.

Si les deux utilisateurs sont déjà amis ou si une demande est déjà en attente :

```txt
Serveur -> Client : 409,F_ADD,Relation déjà existante
```

Sinon, le serveur crée une ligne dans la table `Friend`.

```txt
src_user = User
dst_user = luc
status = PENDING
seen_src = 1
seen_dst = 0
```

`seen_dst = 0` signifie que Luc n’a pas encore vu la demande.

Si la création échoue :

```txt
Serveur -> Client : 405,F_ADD,Erreur création demande ami
```

Si la création réussit :

```txt
Serveur -> Client : 200,F_ADD,Demande envoyée
```

Luc recevra la demande lors de son prochain appel `Update`.

---

## Acceptation ou refus d’une demande d’ami

### Commande

```txt
F_acc,Src_User,Dst_User,0 or 1
```

Dans cette commande :

* `Src_User` est l’utilisateur qui répond à la demande.
* `Dst_User` est l’utilisateur qui avait envoyé la demande.
* `1` signifie accepter.
* `0` signifie refuser.

Exemple :

```txt
Client -> Serveur : F_Acc,luc,User,1
```

Ici, Luc accepte la demande envoyée par User.

Le serveur vérifie que les deux utilisateurs existent.

Si un utilisateur n’existe pas :

```txt
Serveur -> Client : 404,F_ACC,Utilisateur inexistant
```

Le serveur vérifie qu’une demande existe bien dans la table `Friend`.

```txt
src_user = User
dst_user = luc
status = PENDING
```

Si aucune demande n’existe :

```txt
Serveur -> Client : 404,F_ACC,Demande inexistante
```

Si la valeur est différente de `0` ou `1` :

```txt
Serveur -> Client : 400,F_ACC,Valeur invalide
```

Si la valeur est `1`, le serveur accepte la demande :

```txt
status = ACCEPTED
seen_src = 0
seen_dst = 1
```

`seen_src = 0` permet à User de recevoir l’information lors de son prochain `Update`.

Si la valeur est `0`, le serveur refuse la demande :

```txt
status = REFUSED
seen_src = 0
seen_dst = 1
```

Si la modification échoue :

```txt
Serveur -> Client : 405,F_ACC,Erreur traitement demande
```

Si la modification réussit :

```txt
Serveur -> Client : 200,F_ACC,Réponse enregistrée
```

---

## Création d’un groupe

### Commande

```txt
G_add,Src_User,G_Name
```

### Échange client / serveur

Client demande la création d’un groupe.

```txt
Client -> Serveur : G_add,User,ProjetSAE
```

Le serveur vérifie que `Src_User` existe.

Si l’utilisateur n’existe pas :

```txt
Serveur -> Client : 404,G_ADD,Utilisateur inexistant
```

Le serveur vérifie que le nom du groupe est valide.

Si le nom du groupe contient des caractères interdits :

```txt
Serveur -> Client : 400,G_ADD,Nom de groupe invalide
```

Le serveur vérifie si le groupe existe déjà.

Si le groupe existe déjà :

```txt
Serveur -> Client : 409,G_ADD,Groupe déjà existant
```

Sinon, le serveur crée le groupe dans la table `Group`.

```txt
group_name = ProjetSAE
owner = User
```

Puis le serveur ajoute auUseratiquement User dans la table `Group_Member`.

```txt
id_user = User
role = OWNER
seen_join = 1
last_seen_msg_id = 0
```

Si la création échoue :

```txt
Serveur -> Client : 405,G_ADD,Erreur création groupe
```

Si la création réussit :

```txt
Serveur -> Client : 201,G_ADD,Groupe créé
```

---

## Ajout d’un membre dans un groupe

### Commande

```txt
G_Add_M,Src_User,G_Name,User
```

### Échange client / serveur

Client demande à ajouter un utilisateur dans un groupe.

```txt
Client -> Serveur : G_Add_M,User,ProjetSAE,luc
```

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
Serveur -> Client : 404,G_ADD_M,Utilisateur source inexistant
```

Le serveur vérifie que le groupe existe.

Si le groupe n’existe pas :

```txt
Serveur -> Client : 404,G_ADD_M,Groupe inexistant
```

Le serveur vérifie que l’utilisateur à ajouter existe.

Si `User` n’existe pas :

```txt
Serveur -> Client : 404,G_ADD_M,Utilisateur à ajouter inexistant
```

Le serveur vérifie que `Src_User` a le droit d’ajouter un membre.

Par exemple, il doit avoir le rôle `OWNER` ou `ADMIN`.

Si l’utilisateur n’a pas les droits :

```txt
Serveur -> Client : 401,G_ADD_M,Accès refusé
```

Le serveur vérifie que `User` n’est pas déjà membre du groupe.

Si l’utilisateur est déjà membre :

```txt
Serveur -> Client : 409,G_ADD_M,Utilisateur déjà membre
```

Sinon, le serveur ajoute l’utilisateur dans la table `Group_Member`.

```txt
id_group = ProjetSAE
id_user = luc
role = MEMBER
seen_join = 0
last_seen_msg_id = dernier message actuel du groupe
```

`seen_join = 0` permet à Luc de recevoir l’information lors de son prochain `Update`.

Si l’ajout échoue :

```txt
Serveur -> Client : 405,G_ADD_M,Erreur ajout membre
```

Si l’ajout réussit :

```txt
Serveur -> Client : 200,G_ADD_M,Membre ajouté
```

---

## Envoi d’un message dans un groupe

### Commande

```txt
Send_G_Msg,Src_User,G_Name,Msg
```

### Échange client / serveur

Client envoie un message dans un groupe.

```txt
Client -> Serveur : Send_G_Msg,User,ProjetSAE,Salut le groupe
```

Le serveur vérifie que `Src_User` existe.

Si l’utilisateur n’existe pas :

```txt
Serveur -> Client : 404,SEND_G_MSG,Utilisateur inexistant
```

Le serveur vérifie que le groupe existe.

Si le groupe n’existe pas :

```txt
Serveur -> Client : 404,SEND_G_MSG,Groupe inexistant
```

Le serveur vérifie que `Src_User` appartient au groupe.

Si l’utilisateur n’est pas membre du groupe :

```txt
Serveur -> Client : 401,SEND_G_MSG,Utilisateur non membre du groupe
```

Le serveur vérifie que le message n’est pas vide.

Si le message est invalide :

```txt
Serveur -> Client : 400,SEND_G_MSG,Message invalide
```

Le serveur stocke le message dans la table `Message`.

```txt
src_user = User
dst_group = ProjetSAE
type = GROUP
content = Salut le groupe
```

Il n’y a pas besoin de table `update`.

Les autres membres recevront le message lors de leur prochain `Update`, car leur attribut `last_seen_msg_id` dans `Group_Member` sera inférieur à l’identifiant du nouveau message.

Si le stockage échoue :

```txt
Serveur -> Client : 405,SEND_G_MSG,Erreur stockage message groupe
```

Si le stockage réussit :

```txt
Serveur -> Client : 200,SEND_G_MSG,Message de groupe envoyé
```

---

## Exemple complet d’un échange

User envoie une demande d’ami à Luc.

```txt
Client User -> Serveur : F_add,User,luc
Serveur -> Client User : 200,F_ADD,Demande envoyée
```

Dans la base de données :

```txt
src_user = User
dst_user = luc
status = PENDING
seen_src = 1
seen_dst = 0
```

Luc fait un update.

```txt
Client Luc -> Serveur : Update,luc
Serveur -> Client Luc : 200,UPDATE,FRIEND_REQUEST,User
```

Le serveur passe ensuite :

```txt
seen_dst = 1
```

Luc accepte.

```txt
Client Luc -> Serveur : F_Acc,luc,User,1
Serveur -> Client Luc : 200,F_ACC,Réponse enregistrée
```

Dans la base :

```txt
status = ACCEPTED
seen_src = 0
seen_dst = 1
```

User fait un update.

```txt
Client User -> Serveur : Update,User
Serveur -> Client User : 200,UPDATE,FRIEND_ACCEPTED,luc
```

Le serveur passe ensuite :

```txt
seen_src = 1
```

Aucune table `update` n’est utilisée.
