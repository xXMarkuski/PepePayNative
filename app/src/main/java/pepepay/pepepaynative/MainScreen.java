package pepepay.pepepaynative;

public class MainScreen { /* implements Screen, Wallets.WalletsListener {

    private Stage stage;
    private VisScrollPane pane;
    private boolean showloading = false;

    private AnimatedSprite loading;

    private ArrayMap<Wallet, VisTextButton> walletButtons;

    private PepePayDialogHandler handler;
    private VisTextButton openOptions;

    private int currentButton;

    public void addWallet(final Wallet wallet) {
        Group group = ((Group) pane.getUserObject());
        final VisTextButton button = new VisTextButton("");
        final PepePayDialog window = new WalletDialog(wallet);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handler.setDialog(window);
                currentButton = walletButtons.indexOfValue(button, false) + 1;
            }
        });
        button.setUserObject(wallet);
        button.pad(button.getLabel().getStyle().font.getSpaceWidth());
        button.setProgrammaticChangeEvents(true);
        group.addActor(button);
        walletButtons.put(wallet, button);
        updateNames();
    }

    public void addWallets(Array<Wallet> wallets) {
        for (Wallet wallet : wallets) {
            this.addWallet(wallet);
        }
    }

    public void updateNames() {
        for (Actor actor : ((Group) pane.getUserObject()).getChildren()) {
            Wallet wallet = (Wallet) actor.getUserObject();
            if (wallet != null) {
                ((VisTextButton) actor).setText(Wallets.getName(wallet));
            }
        }
    }


    @Override
    public void show() {
        ScreenViewport viewport = new ScreenViewport();
        //viewport.setUnitsPerPixel(0.33F);

        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        Group group = new HorizontalGroup();
        pane = new VisScrollPane(group);
        pane.setUserObject(group);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(false, true);
        stage.addActor(pane);

        handler = new PepePayDialogHandler(stage);

        final VisTextButton addWalletButton = new VisTextButton("Add Wallet");
        addWalletButton.setColor(Color.GREEN);
        addWalletButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (handler.getDialog() instanceof WalletCreationDialog) return;
                handler.setDialog(new WalletCreationDialog());
                currentButton = 0;
            }
        });
        addWalletButton.pad(addWalletButton.getLabel().getStyle().font.getSpaceWidth());
        group.addActor(addWalletButton);

        openOptions = new VisTextButton("O");
        final ClickListener listener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (handler.getDialog() instanceof OptionsDialog) return;
                PepePayDialog dialog = handler.getDialog();
                handler.setDialog(new OptionsDialog("Options"));
                handler.setNextDialog(dialog);
                currentButton = -1;
            }
        };
        openOptions.addListener(listener);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.MENU) {
                    listener.clicked(null, 0, 0);
                    return true;
                }
                return super.keyUp(event, keycode);
            }
        });
        stage.addActor(openOptions);


        FileHandle root = Gdx.files.internal("ui/loading");
        Array<TextureRegion> pics = new Array<TextureRegion>(24);
        for (int i = 1; i < 25; i++) {
            pics.add(new TextureRegion(new Texture(root.child(i + ".png"))));
        }
        Animation anime = new Animation(0.05F, pics, Animation.PlayMode.LOOP);
        loading = new AnimatedSprite(anime);
        loading.setSize(Gdx.graphics.getPpcX(), Gdx.graphics.getPpcY());

        this.walletButtons = new ArrayMap<Wallet, VisTextButton>();

        Wallets.addWalletAddListener(this);
        Array<Wallet> wallets = Wallets.getOwnWallets();
        addWallets(wallets);

        PepePayDialog walletDialog;

        if (wallets.size > 0) {
            walletDialog = new WalletDialog(wallets.first());
            currentButton = 1;
        } else {
            walletDialog = new WalletCreationDialog();
            currentButton = 0;
        }
        handler.setDialog(walletDialog);

        stage.addListener(new ActorGestureListener() {
            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                System.out.println("filing" + currentButton);
                if (velocityX > 0) {
                    if (currentButton > 0) {
                        for (EventListener listener1 : ((HorizontalGroup) pane.getChildren().get(0)).getChildren().get(currentButton - 1).getListeners()) {
                            if (listener1 instanceof ClickListener) {
                                ((ClickListener) listener1).clicked(null, 0, 0);
                            }
                        }
                    }
                } else {
                    if (currentButton < pane.getChildren().size + 1) {
                        for (EventListener listener1 : ((HorizontalGroup) pane.getChildren().get(0)).getChildren().get(currentButton + 1).getListeners()) {
                            if (listener1 instanceof ClickListener) {
                                ((ClickListener) listener1).clicked(null, 0, 0);
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        positionLogic(stage.getWidth(), stage.getHeight());
        PepePay.CONNECTION_MANAGER.update();
        try {
            stage.draw();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        loadingLogic();
    }

    private void loadingLogic() {
        if (showloading) {
            loading.setPosition(50, 50 + PepePay.KEYBOARD_MANIPULATION.getSoftKeyboardHeight() + Gdx.graphics.getPpcY());
            stage.getBatch().begin();
            loading.draw(stage.getBatch());
            stage.getBatch().end();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    private void positionLogic(float screenWidth, float screenHight) {
        openOptions.setPosition(screenWidth - openOptions.getWidth(), screenHight - openOptions.getHeight());
        pane.setWidth(screenWidth - openOptions.getWidth());

        Group group = ((Group) pane.getUserObject());
        group.setHeight(openOptions.getHeight());
        float addHeight = 0;
        if (pane.isScrollX()) {
            addHeight = 2 * pane.getScrollBarHeight() - pane.getStyle().hScrollKnob.getBottomHeight();
        }
        pane.setHeight(group.getHeight() + addHeight);

        pane.setY(screenHight - pane.getHeight());

        int keyboardHeight = PepePay.KEYBOARD_MANIPULATION.getSoftKeyboardHeight();
        handler.resize(screenWidth, screenHight - pane.getHeight() - keyboardHeight);
        handler.setY(keyboardHeight);
    }

    @Override
    public void pause() {
        PepePay.CONNECTION_MANAGER.onPause();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Wallets.saveAll();
                PepePay.OPTIONS.save(PepePay.optionsFileHandle);
            }
        }).start();
    }

    @Override
    public void resume() {
        PepePay.CONNECTION_MANAGER.onResume();
    }

    @Override
    public void hide() {
    }

    //This Function is never called - oh well
    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void privateWalletAdded(Wallet wallet) {
        addWallet(wallet);
        showloading = false;
        handler.setDialog(new WalletDialog(wallet));
    }

    @Override
    public void privateWalletGeneratingBegin() {
        showloading = true;
    }

    @Override
    public void nameChange(String walletID, String newName) {
        VisTextButton button = walletButtons.get(Wallets.getWallet(walletID));
        if (button != null) {
            button.setText(newName);
        }
    }

    @Override
    public void balanceChange(String walletID, float newBalance) {

    }*/
}
