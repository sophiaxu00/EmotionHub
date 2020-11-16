This folder contains work of team "unicorns of love" -- Chenyu Zhang, Jinhan Zhang, Shravan Venkatesan, Sophia Xu, Xi He, and Lynn Yin for the fall 2020 OOSE class. 

The `homework` folder contains the course homeworks. The `EmotionalHub` folder contains our main project, and the `docs` folder contains relevant documentation.

## EmotionalHub

This is the source code for a web application that features an "emotionally conscious" social platform, where users would receive feeds that fit their emotional needs, and they will also be able to make posts themselves.

After logging in, the user would be prompted to a page where they can select one of "cheer me up," "chill," "inspire me," "care for my emotion" (where they in turn need to select an emotion), "skip," etc. They will then see contents (only text currently) placed in blocks, posted by other users, which are taylored towards the intentions they selected, using our machine learning algorithm. They would be able to make posts about anything in the empty blocks. Various forms of media including images and video will soon be supported, but file uploads are not currently functional (see Issue #28). 

(Not implemented yet) The user would also be able to open a thread on an emotionally-aware forum so that they can ask questions or express myself. They can follow up on a thread to answer other people's questions, help with their concerns, share their happiness, etc.

The app is currently deployed with Heroku at [https://emotional-hub.herokuapp.com/](https://emotional-hub.herokuapp.com/).

To run this application locally, run the `Server.java` file under `src/min/java/server`. Then the app would be running on `localhost:7000`. 

You can use the account with email `testing@jhu.edu` and password `testing` for testing. 

### Emotion tagging models and post recommendation system (prototype done)

We combine two machine learning models (VAD model and CrowdFlower model) to detect emotions from posts. Both of they are trained now. The post recommendation algorithm will connect the emotions detected with user intentions. The current version of this recommendation algorithm can be found in [this issue](https://github.com/jhu-oose/2020-fall-group-unicorns-of-love/issues/25), which will be further optimized.

In the application, this algorithm is presented as the connection between the home page (where the user selects their intention) to the feed page, where the user sees posts from other users. Different chosen intentions bring up different posts which correspond to that intention as predicted by the algorithm.

#### VAD model

**Training:** We first trained models with EmoBank data to give valence-arousal-dominance (VAD) scores to posts. The pretrained T5 model is used. Check [this colab notebook](https://colab.research.google.com/drive/1Hv3Rl7qRjVO31feJ4z2cNE7hNHLfIn6J?usp=sharing) to see how the model was trained. Details and citations can be found in [this issue](https://github.com/jhu-oose/2020-fall-group-unicorns-of-love/issues/18).

**Deployment:** When running our app locally, if you make a post, you will be able to see VAD scores of your post printed in the Java console.

In the current version, we have deployed the ML models on a separate endpoint and, in our app, we fetch prediction results from that endpoint. The endpoint is built with Flask, and the models themselves are served on Google AI platform. Details can be found in [this issue](https://github.com/jhu-oose/2020-fall-group-unicorns-of-love/issues/23).

The `vad-predict-app` is the model application. It is included in this repo as a submodule. To see the code in the submodule after pulling this repo, use `git submodule init`. There is a simple frontend made for testing the model API endpoint.

Try the model endpoint here: https://vad-predict.herokuapp.com/

> Example 1:  “It is a great day.” -> 
{"results":{"arousal":"3.44","dominance":"3.2","valence":"4.0"}}

**VAD Score Explanation** On a scale of 1 to 5, 
- for Arousal, 1 means calm, and 5 means excited;  
- for Dominance, 1 means being controlled, and 5 means in control; 
- for Valence, 1 means negative, 5 means positive.

#### CrowdFlower Model （this is still to be incorporated in the model endpoint)

- Dataset: [CrowdFlower](https://data.world/crowdflower/sentiment-analysis-in-text) (modified) (labelled with 5 possible emotion tags: happy, sad, hate, anger, and neutral)
- Model: multi-channel model (CNN+LSTM) adapted from this [repo](https://github.com/tlkh/text-emotion-classification)
- Training Code and testing example can be found in this [colab notebook](https://colab.research.google.com/drive/1n_78Z5-ygEEtnK8CYFP7m-syrxr1heuj)
- Not yet deployed. (Goal for iteration 4)


We also incorporate another emotional model trained on CrowdFlower dataset, which contains 40,000 tweets labeled with 13 different emotion tags. A simplified version of the dataset was commonly used by researchers. The modified dataset contains 5 emotion labels (happy, sad, hate, anger, and neutral). We found a well-designed deep learning model that achieved high accuracy in this emotion classification task. Since the dataset required to train the model is too large and can't be pushed to the repo, we will only upload and deploy the well-trained model, which will label posts together with the VAD tagging model mentioned above. The training code can be found in this .

### Database

User and Post information is stored in mongoDB. The DB access information needs to be removed from the code if this repo becomes public. 
