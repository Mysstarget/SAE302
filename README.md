# SAE302 — Méthodes du protocole

## Codes serveur utilisés

* `200` : succès / opération réussie
* `201` : création réussie
* `400` : requête invalide / caractères spéciaux refusés
* `401` : mauvais mot de passe
* `402` : erreur lors de la récupération des données
* `404` : utilisateur, groupe ou ressource inexistante
* `405` : erreur serveur lors de la création, suppression, stockage ou mise à jour
* `409` : conflit, ressource déjà existante

---

## Création utilisateur

### `Create_User`

Commande envoyée :

```txt
Create,User,password
```

Client envoie :

```txt
Create,user,password
```

Serveur vérifie le nom de l’utilisateur.

Si le nom contient des caractères spéciaux refusés :

```txt
400
```

Si le nom est valide, le serveur vérifie si l’utilisateur existe déjà.

Si l’utilisateur existe déjà :

```txt
409
```

Si l’utilisateur n’existe pas, le serveur tente de créer l’utilisateur.

Si la création échoue :

```txt
405
```

Si la création réussit :

```txt
201
```

Le serveur renvoie le code au client.

Le client traite le code et affiche un message clair à l’utilisateur.

---

## Connexion utilisateur

### `connect_utilisateur`

Commande envoyée :

```txt
Connect,user,password
```

Le serveur vérifie que l’utilisateur existe.

Si l’utilisateur n’existe pas :

```txt
404
```

Si l’utilisateur existe, le serveur vérifie le mot de passe.

Si le mot de passe est incorrect :

```txt
401
```

Si le mot de passe est correct :

```txt
200
```

Le serveur récupère ensuite les données de l’utilisateur :

* amis
* groupes
* messages
* demandes en attente
* mises à jour

Si la récupération échoue :

```txt
402
```

Si tout est correct, le serveur envoie les données au client en CSV.

Le client traite les données et les affiche proprement.

---

## Suppression utilisateur

### `Delete_User`

Commande envoyée :

```txt
Delete,User,password
```

Le serveur vérifie que l’utilisateur existe.

Si l’utilisateur n’existe pas :

```txt
404
```

Si l’utilisateur existe, le serveur vérifie le mot de passe.

Si le mot de passe est incorrect :

```txt
401
```

Si le mot de passe est correct :

```txt
200
```

Le serveur supprime ensuite l’utilisateur dans les tables concernées :

* table utilisateur
* table amis
* table groupes
* table appartenance groupe
* table messages
* table update

Si la suppression échoue :

```txt
405
```

Si la suppression réussit :

```txt
200
```

Le serveur stocke ensuite l’information dans la table update pour prévenir les utilisateurs concernés au prochain update.

---

## Mise à jour utilisateur

### `update`

Commande envoyée :

```txt
Update,user
```

Le serveur vérifie que l’utilisateur existe.

Si l’utilisateur n’existe pas :

```txt
404
```

Si l’utilisateur existe :

```txt
200
```

Le serveur regarde ensuite la table update.

S’il n’y a aucune nouvelle donnée :

```txt
200
```

Le serveur renvoie un CSV vide ou un message indiquant qu’il n’y a pas de mise à jour.

S’il y a des données, le serveur les récupère.

Si la récupération échoue :

```txt
402
```

Si la récupération réussit, le serveur renvoie les données au client en CSV.

Le client traite les données reçues.

Après l’envoi, le serveur peut supprimer les updates déjà envoyés.

---

## Envoi de message privé

### `send_Msg`

Commande envoyée :

```txt
Send_Msg,Src_User,Dst_User,msg
```

Le serveur vérifie que l’utilisateur source existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que l’utilisateur destination existe.

Si `Dst_User` n’existe pas :

```txt
404
```

Le serveur vérifie que le message n’est pas vide et ne contient pas de caractères interdits.

Si le message est invalide :

```txt
400
```

Le serveur tente de stocker le message dans la table message.

Si le stockage échoue :

```txt
405
```

Si le message est stocké correctement :

```txt
200
```

Le serveur ajoute ensuite une entrée dans la table update pour prévenir `Dst_User` qu’il a reçu un nouveau message.

Si l’ajout dans la table update échoue :

```txt
405
```

Si tout est correct, le serveur renvoie au client :

```txt
200
```

Le client affiche un message indiquant que le message a bien été envoyé.

---

## Ajout d’ami

### `freinds_add`

Commande envoyée :

```txt
F_add,Src_User,Dst_User
```

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que `Dst_User` existe.

Si `Dst_User` n’existe pas :

```txt
404
```

Le serveur vérifie que `Src_User` et `Dst_User` ne sont pas identiques.

Si l’utilisateur essaie de s’ajouter lui-même :

```txt
400
```

Le serveur vérifie si les deux utilisateurs sont déjà amis.

S’ils sont déjà amis :

```txt
409
```

Le serveur vérifie si une demande d’ami existe déjà.

Si une demande existe déjà :

```txt
409
```

Le serveur crée une demande d’ami en attente.

Si la création échoue :

```txt
405
```

Si la demande est créée correctement :

```txt
200
```

Le serveur ajoute une entrée dans la table update pour prévenir `Dst_User` qu’il a reçu une demande d’ami.

Le client affiche un message indiquant que la demande a bien été envoyée.

---

## Acceptation ou refus d’une demande d’ami

### `freind_acc`

Commande envoyée :

```txt
F_Acc,Src_User,Dst_User,0or1
```

`0` signifie refus de la demande.
`1` signifie acceptation de la demande.

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que `Dst_User` existe.

Si `Dst_User` n’existe pas :

```txt
404
```

Le serveur vérifie qu’une demande d’ami existe entre les deux utilisateurs.

Si aucune demande n’existe :

```txt
404
```

Si la valeur reçue est différente de `0` ou `1` :

```txt
400
```

Si la valeur est `0`, le serveur refuse la demande et la supprime.

Si la suppression échoue :

```txt
405
```

Si le refus est bien enregistré :

```txt
200
```

Le serveur ajoute une update pour prévenir l’autre utilisateur du refus.

Si la valeur est `1`, le serveur accepte la demande.

Le serveur ajoute les deux utilisateurs dans la table amis.

Si l’ajout échoue :

```txt
405
```

Le serveur supprime ensuite la demande d’ami en attente.

Si l’opération réussit :

```txt
200
```

Le serveur ajoute une update pour prévenir les deux utilisateurs que la demande a été acceptée.

Le client traite le code et affiche un message clair.

---

## Création d’un groupe

### `Group_Add`

Commande conseillée :

```txt
G_add,Src_User,G_Name
```

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que le nom du groupe ne contient pas de caractères spéciaux interdits.

Si le nom est invalide :

```txt
400
```

Le serveur vérifie si le groupe existe déjà.

Si le groupe existe déjà :

```txt
409
```

Le serveur crée le groupe.

Si la création échoue :

```txt
405
```

Si la création réussit :

```txt
201
```

Le serveur ajoute automatiquement `Src_User` comme membre du groupe, éventuellement avec le rôle administrateur ou créateur.

Si l’ajout du créateur dans le groupe échoue :

```txt
405
```

Si tout est correct, le serveur renvoie :

```txt
201
```

Le client affiche un message indiquant que le groupe a bien été créé.

---

## Ajout d’un membre dans un groupe

### `Group_Add_Member`

Commande conseillée :

```txt
G_Add_M,Src_User,G_Name,User
```

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que le groupe existe.

Si le groupe n’existe pas :

```txt
404
```

Le serveur vérifie que l’utilisateur à ajouter existe.

Si `User` n’existe pas :

```txt
404
```

Le serveur vérifie que `Src_User` appartient au groupe ou a le droit d’ajouter un membre.

Si `Src_User` n’a pas le droit :

```txt
401
```

Le serveur vérifie que `User` n’est pas déjà membre du groupe.

Si `User` est déjà membre :

```txt
409
```

Le serveur ajoute `User` dans le groupe.

Si l’ajout échoue :

```txt
405
```

Si l’ajout réussit :

```txt
200
```

Le serveur ajoute une update pour prévenir `User` qu’il a été ajouté au groupe.

Le serveur peut aussi ajouter une update pour les autres membres du groupe afin de mettre à jour la liste des membres.

Le client affiche un message indiquant que l’utilisateur a bien été ajouté au groupe.

---

## Envoi d’un message dans un groupe

### `Send_Group_Msg`

Commande conseillée :

```txt
Send_G_Msg,Src_User,G_Name,Msg
```

Le serveur vérifie que `Src_User` existe.

Si `Src_User` n’existe pas :

```txt
404
```

Le serveur vérifie que le groupe existe.

Si le groupe n’existe pas :

```txt
404
```

Le serveur vérifie que `Src_User` appartient au groupe.

Si `Src_User` n’est pas membre du groupe :

```txt
401
```

Le serveur vérifie que le message n’est pas vide et ne contient pas de caractères interdits.

Si le message est invalide :

```txt
400
```

Le serveur stocke le message dans la table message avec le nom du groupe comme destination.

Si le stockage échoue :

```txt
405
```

Si le stockage réussit :

```txt
200
```

Le serveur récupère la liste des membres du groupe.

Si la récupération échoue :

```txt
402
```

Le serveur ajoute une entrée dans la table update pour chaque membre du groupe, sauf éventuellement `Src_User`.

Si l’ajout dans la table update échoue :

```txt
405
```

Si tout est correct, le serveur renvoie :

```txt
200
```

Le client affiche un message indiquant que le message de groupe a bien été envoyé.

---

## Format CSV possible pour les réponses serveur

Exemple de réponse simple :

```txt
200,Message envoyé
```

Exemple de réponse avec erreur :

```txt
404,Utilisateur inexistant
```

Exemple de réponse update avec message :

```txt
UPDATE,MSG,Src_User,Dst_User,Message
```

Exemple de réponse update avec demande d’ami :

```txt
UPDATE,F_REQUEST,Src_User,Dst_User
```

Exemple de réponse update avec groupe :

```txt
UPDATE,GROUP_MSG,Src_User,G_Name,Message
```
