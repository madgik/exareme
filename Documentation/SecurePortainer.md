## [Optional] Secure Portainer

As stated in the official <a href="https://portainer.readthedocs.io/en/stable/deployment.html#secure-portainer-using-ssl">Deployment Documentation</a> <br />
**"By default, Portainer’s web interface and API is exposed over HTTP. This is not secured, it’s recommended to enable SSL in a production environment."** <br />

To enable SSL, you need to create an SSL certificate. <br />

For Ubuntu 18.04.3 LTS we **used and tested** ```letsencrypt```. Following the instruction from here: <br />
https://devanswers.co/lets-encrypt-ssl-apache-ubuntu-18-04/ to generate an SSL certificate you need to:

0. Check that ```hostname --fqdn``` contains not only the hostname but the domain name too.
1. Install apache if not already installed. For Ubuntu 18.04.3 LTS we followed the instructions from <a href="https://devanswers.co/installing-apache-ubuntu-18-04-server-virtual-hosts/">here</a><br />
```sudo apt update && sudo apt install apache2```<br/>

2. To see if Apache installed correctly, we can check the current Apache service status.<br />
```sudo service apache2 status``` <br />

3. Install Let’s Encrypt client (Certbot) <br />
```sudo apt-get update && sudo apt-get install software-properties-common``` <br />
```sudo add-apt-repository universe && sudo add-apt-repository ppa:certbot/certbot``` <br />
```sudo apt-get update && sudo apt-get install certbot python-certbot-apache``` <br />

Press Enter or Yes when prompted to continue.

4. Get an SSL Certificate <br />
```sudo certbot --apache```
<br />

```
Enter email address (used for urgent renewal and security notices) (Enter 'c' to cancel):
```

Enter an email address where you can be contacted in case of urgent renewal and security notices. <br />

```
Please read the Terms of Service at
https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf. You must
agree in order to register with the ACME server at
https://acme-v02.api.letsencrypt.org/directory
```

Press a and ENTER to agree to the Terms of Service.<br />

```
Would you be willing to share your email address with the Electronic Frontier
Foundation, a founding partner of the Let's Encrypt project and the non-profit
organization that develops Certbot? We'd like to send you email about EFF and
our work to encrypt the web, protect its users and defend digital rights.
```

Press n and ENTER to not share your email address with EFF. <br />

```
Which names would you like to activate HTTPS for?
```

If you do not already have a list with names, you need to add a Domain name for your Portainer service.<br />
One suggestion is to give such name: <br />
```hostname --fqdn```<br />
by executing the corresponding command in the machine you want to run Portainer.

Make sure you remember the Domain name because you will be prompted to enter it while executing ```deployLocal.sh``` script.

If you do have a list with names:
Select option 1 if you don’t want to use the www. prefix in your website address, otherwise select option 2.
<br />

```
Obtaining a new certificate......
```

Press 1 and ENTER to No redirect - Make no further changes to the webserver configuration. <br />

*The SSL certificate just created.* <br />

**Keep in mind that ```letsencrypt``` certifications expire after 90 days** <br />
