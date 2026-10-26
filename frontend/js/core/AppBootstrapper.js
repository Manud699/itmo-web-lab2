import { ValidateForm } from '../validators/ValidateForm.js';
import { OptionsValidator } from '../validators/OptionsValidator.js';
import { RangeValidator } from '../validators/RangeValidator.js';
import { LocalStorageRep } from '../repository/LocalStorageRep.js';
import { CoordinatePlaneRenderer } from '../draw/CoordinatePlaneRenderer.js';
import { FormRendererFacade } from '../facade/FormRendererFacade.js';
import { AppStore } from './AppStore.js';
import { History } from './History.js';
import { AppRenderer } from '../view/AppRenderer.js';

export class AppBootstrapper {
    #fieldValidators;
    #formValidator;
    #repository;
    #store;
    #history;
    #renderer;
    #controller;

    boot() {
        this.#initValidators();
        this.#initModel();
        this.#initView();
        this.#initController();
        this.#connectObservers();
        this.#restoreOrStart();     
        this.#controller.setupEventListeners();
    }

    #initValidators() {
        this.#fieldValidators = {
            x: new OptionsValidator([-3, -2, -1, 0, 1, 2, 3, 4, 5]),
            y: new RangeValidator(-5, 3),
            r: new OptionsValidator([1, 1.5, 2, 2.5, 3])
        };
        const { x, y, r } = this.#fieldValidators;
        this.#formValidator = new ValidateForm(x, y, r);
    }

    #initModel() {
        this.#repository = new LocalStorageRep('lab1_history');
        this.#store = new AppStore();
        this.#history = new History();
    }

    #initView() {
        const plane = new CoordinatePlaneRenderer('canvas');
        this.#renderer = new AppRenderer(plane);
    }

    #initController() {
        this.#controller = new FormRendererFacade(
            this.#store,
            this.#history,
            this.#formValidator,
            this.#fieldValidators
        );
    }

    #connectObservers() {
        this.#store.subscribe(state => this.#renderer.render(state));

        this.#history.subscribe(view => {
            this.#renderer.renderLog(view, {
                onRestore: id => this.#controller.restore(id),
                onRestoreBranch: (branchId, id) => this.#controller.restoreFromBranch(branchId, id)
            });
            this.#repository.save(this.#history);
        });
    }

    #restoreOrStart() {
        this.#history.load(this.#repository.load());
        const current = this.#history.current();

        if (current) {
            this.#store.restoreMemento(current.memento);
        } else {
            this.#history.record('INIT', null, null, this.#store.createMemento());
            this.#renderer.render(this.#store.getState());
        }
    }
}
