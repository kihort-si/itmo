import {bootstrapApplication} from '@angular/platform-browser'
import {appConfig} from './app/app.config'
import {AppComponent} from './app/layout/app/app.component'

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));

(window as any).updateGraphScale = function (rValue: number) {
    const graph = document.getElementById('graph') as HTMLElement;
    if (graph) {
        requestAnimationFrame(() => {
            if (rValue && Number(rValue) > 0) {
                graph.style.transform = `scale(${Number(rValue) / 5})`;
            } else {
                graph.style.transform = 'scale(0)';
            }
        });
    }
};

