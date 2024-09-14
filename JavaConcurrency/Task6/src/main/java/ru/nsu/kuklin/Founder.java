package ru.nsu.kuklin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class Founder {
    private final List<Runnable> workers;
    private CyclicBarrier calculationBarrier;
    public Founder(final Company company) {
        this.workers = new ArrayList<>(company.getDepartmentsCount());
        calculationBarrier = new CyclicBarrier(company.getDepartmentsCount(), company::showCollaborativeResult);
        for (int i = 0; i < company.getDepartmentsCount(); i++) {
            final int departmentId = i;
            workers.add(() -> {
                var department = company.getFreeDepartment(departmentId);
                department.performCalculations();
                try {
                    calculationBarrier.await();
                } catch (InterruptedException e) {
                    System.out.printf("Department %d was interrupted while waiting on barrier\n", departmentId);
                } catch (BrokenBarrierException e) {
                    System.out.println(e);
                }
            });
        }
    }
    public void start() {
        for (final Runnable worker : workers) {
            new Thread(worker).start();
        }
    }
}